import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;

/** Checks the local JDBC connection and prints a few seed row counts. */
public class MysqlCon {
    public static void main(String[] args) throws Exception {
        String url = System.getenv().getOrDefault("DB_URL", "jdbc:mysql://127.0.0.1:3306/pawmatch");
        String user = System.getenv().getOrDefault("DB_USER", "root");
        String password = System.getenv("DB_PASSWORD");
        if (password == null) {
            if (System.console() == null) {
                throw new IllegalStateException("Set DB_PASSWORD in the run configuration, or run from a terminal.");
            }
            char[] input = System.console().readPassword("MySQL password: ");
            if (input == null) {
                throw new IllegalStateException("No MySQL password was provided.");
            }
            password = new String(input);
            Arrays.fill(input, '\0');
        }

        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(
                     "SELECT 'Owners' AS table_name, COUNT(*) AS row_count FROM Owners "
                     + "UNION ALL SELECT 'Pets', COUNT(*) FROM Pets "
                     + "UNION ALL SELECT 'Bookings', COUNT(*) FROM Bookings")) {
            System.out.println("Connected to " + connection.getCatalog());
            while (rows.next()) {
                System.out.printf("%s: %d%n", rows.getString("table_name"), rows.getInt("row_count"));
            }
        }
    }
}
