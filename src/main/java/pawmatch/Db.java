package pawmatch;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Db {
    private static final Properties SETTINGS = loadSettings();

    private static Properties loadSettings() {
        Properties settings = new Properties();
        String file = System.getProperty("pawmatch.config",
                System.getenv().getOrDefault("PAWMATCH_CONFIG", ".env"));
        if (Files.exists(Path.of(file))) {
            try (InputStream input = Files.newInputStream(Path.of(file))) {
                settings.load(new java.io.InputStreamReader(input, StandardCharsets.UTF_8));
            } catch (IOException error) {
                throw new IllegalStateException("Could not read local database settings.", error);
            }
        }
        return settings;
    }

    public static String setting(String key, String fallback) {
        return System.getenv().getOrDefault(key, SETTINGS.getProperty(key, fallback));
    }

    public static Connection open() throws SQLException {
        return connect("pawmatch");
    }

    private static Connection connect(String database) throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException error) {
            throw new SQLException("MySQL JDBC driver is unavailable.", error);
        }

        String password = setting("DB_PASSWORD", null);
        if (password == null) {
            throw new SQLException("Set DB_PASSWORD in .env or the environment. See .env.example.");
        }
        String url = "jdbc:mysql://" + setting("DB_HOST", "127.0.0.1") + ":"
                + setting("DB_PORT", "3306") + "/" + database + "?serverTimezone=UTC";
        return DriverManager.getConnection(url, setting("DB_USER", "root"), password);
    }

    public static String initialize() throws SQLException, IOException {
        String schema = resource("schema.sql");
        Map<String, String> tables = new LinkedHashMap<>();
        Matcher definitions = Pattern.compile("(?s)CREATE TABLE (\\w+) \\(.*?\\n\\);").matcher(schema);
        while (definitions.find()) {
            tables.put(definitions.group(1), definitions.group());
        }
        if (tables.size() != 14) {
            throw new IOException("The supplied 14-table schema is missing or incomplete.");
        }

        try (Connection server = connect(""); Statement statement = server.createStatement()) {
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS pawmatch CHARACTER SET utf8mb4");
        }
        try (Connection connection = open()) {
            List<String> existing = new ArrayList<>();
            try (ResultSet rows = connection.getMetaData().getTables("pawmatch", null, "%", new String[] {"TABLE"})) {
                while (rows.next()) existing.add(rows.getString("TABLE_NAME"));
            }
            long found = tables.keySet().stream().filter(existing::contains).count();
            if (found != 0 && found != tables.size()) {
                throw new SQLException("PawMatch has a partial schema. Inspect it or explicitly reseed it; startup did not replace it.");
            }
            if (found == 0 && !existing.isEmpty()) {
                throw new SQLException("PawMatch contains unrelated tables. Inspect the database before initializing it.");
            }
            if (found == 0) executeScript(connection, schema);
            for (Map.Entry<String, String> table : tables.entrySet()) {
                checkColumns(connection, table.getKey(), table.getValue());
            }

            String accounts = resource("accounts.sql");
            executeScript(connection, accounts);
            checkColumns(connection, "Accounts", accounts);
            String passwordReset = resource("password_reset.sql");
            executeScript(connection, passwordReset);
            checkColumns(connection, "PasswordResetTokens", passwordReset);

            int empty = 0;
            for (String table : tables.keySet()) {
                try (Statement statement = connection.createStatement();
                     ResultSet rows = statement.executeQuery("SELECT COUNT(*) FROM `" + table + "`")) {
                    rows.next();
                    if (rows.getLong(1) == 0) empty++;
                }
            }
            if (empty != 0 && empty != tables.size()) {
                throw new SQLException("PawMatch is partly populated. Startup kept the data; use the explicit reset command only if intended.");
            }
            if (empty == tables.size()) {
                connection.setAutoCommit(false);
                try {
                    // The seed already wraps its inserts in a transaction.
                    executeScript(connection, resource("seed.sql"));
                    connection.commit();
                } catch (SQLException error) {
                    connection.rollback();
                    throw error;
                } finally {
                    connection.setAutoCommit(true);
                }
                return "Loaded the supplied sample data into empty PawMatch tables.";
            }
            return "Using existing PawMatch data without reseeding.";
        }
    }

    private static void checkColumns(Connection connection, String table, String definition) throws SQLException {
        Map<String, String> actual = new LinkedHashMap<>();
        Map<String, Integer> sizes = new LinkedHashMap<>();
        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery("SELECT * FROM `" + table + "` WHERE 1 = 0")) {
            ResultSetMetaData metadata = rows.getMetaData();
            for (int i = 1; i <= metadata.getColumnCount(); i++) {
                actual.put(metadata.getColumnName(i), metadata.getColumnTypeName(i));
                sizes.put(metadata.getColumnName(i), metadata.getPrecision(i));
            }
        }
        Matcher columns = Pattern.compile("(?m)^\\s+(\\w+)\\s+(INT|VARCHAR|DECIMAL|DATETIME|DATE|BOOLEAN)\\b(?:\\((\\d+)(?:,\\d+)?\\))?").matcher(definition);
        while (columns.find()) {
            String expected = columns.group(2);
            if (expected.equals("BOOLEAN")) expected = "TINYINT";
            String type = actual.get(columns.group(1));
            if (expected.equals("TINYINT") && "BIT".equalsIgnoreCase(type)) type = "TINYINT";
            boolean tooSmall = columns.group(3) != null && sizes.getOrDefault(columns.group(1), 0) < Integer.parseInt(columns.group(3));
            if (type == null || !type.equalsIgnoreCase(expected) || tooSmall) {
                throw new SQLException("Incompatible column " + table + "." + columns.group(1) + "; expected " + expected + ". No data was replaced.");
            }
        }
    }

    private static String resource(String name) throws IOException {
        try (InputStream input = Db.class.getResourceAsStream("/sql/" + name)) {
            if (input == null) throw new IOException("Missing packaged SQL file: " + name);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void executeScript(Connection connection, String text) throws SQLException {
        StringBuilder sql = new StringBuilder();
        char quote = 0;
        try (Statement statement = connection.createStatement()) {
            for (int i = 0; i < text.length(); i++) {
                char ch = text.charAt(i);
                if (quote != 0) {
                    sql.append(ch);
                    if (ch == '\\' && i + 1 < text.length()) sql.append(text.charAt(++i));
                    else if (ch == quote) {
                        if (i + 1 < text.length() && text.charAt(i + 1) == quote) sql.append(text.charAt(++i));
                        else quote = 0;
                    }
                } else if (ch == '\'' || ch == '"' || ch == '`') {
                    quote = ch;
                    sql.append(ch);
                } else if (ch == ';') {
                    if (!sql.toString().isBlank()) statement.execute(sql.toString());
                    sql.setLength(0);
                } else sql.append(ch);
            }
            if (!sql.toString().isBlank()) statement.execute(sql.toString());
        }
    }
}
