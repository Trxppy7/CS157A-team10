package pawmatch;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("title", "Create an account");
        request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
    }

    @Override
    protected synchronized void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!Web.validCsrf(request)) {
            response.setStatus(403);
            request.setAttribute("message", "This form expired. Please try again.");
            doGet(request, response);
            return;
        }
        String role = Web.text(request, "role");
        String name = Web.text(request, "name");
        String email = Web.email(request);
        String phone = Web.text(request, "phone");
        String password = request.getParameter("password");
        Map<String, String> errors = new LinkedHashMap<>();
        if (!role.equals("owner") && !role.equals("caregiver")) errors.put("role", "Choose pet owner or caregiver.");
        if (name.isBlank() || name.length() > 100) errors.put("name", "Enter a name with 1 to 100 characters.");
        if (!Web.validEmail(email)) errors.put("email", "Enter an email address like name@example.com.");
        if (!Web.validPhone(phone)) errors.put("phone", "Enter a phone number with 10 to 15 digits.");
        if (password == null || password.length() < 8 || password.length() > 128) errors.put("password", "Use 8 to 128 characters.");
        if (errors.isEmpty()) {
            String hash = Passwords.hash(password);
            try (Connection connection = Db.open()) {
                connection.setAutoCommit(false);
                try {
                    try (PreparedStatement check = connection.prepareStatement(
                            "SELECT email FROM Accounts WHERE email = ? UNION SELECT email FROM Owners WHERE email = ? UNION SELECT email FROM Caregivers WHERE email = ?")) {
                        for (int i = 1; i <= 3; i++) check.setString(i, email);
                        try (ResultSet rows = check.executeQuery()) {
                            if (rows.next()) throw new SQLException("Email already used", "23000");
                        }
                    }
                    String table = role.equals("owner") ? "Owners" : "Caregivers";
                    String profileIdColumn = role.equals("owner") ? "owner_id" : "caregiver_id";
                    int profileId = nextId(connection, table, profileIdColumn);
                    try (PreparedStatement profile = connection.prepareStatement(
                            "INSERT INTO " + table + " (" + profileIdColumn + ", name, email, phone_number) VALUES (?, ?, ?, ?)")) {
                        profile.setInt(1, profileId);
                        profile.setString(2, name);
                        profile.setString(3, email);
                        profile.setString(4, phone);
                        profile.executeUpdate();
                    }
                    int accountId = nextId(connection, "Accounts", "account_id");
                    try (PreparedStatement account = connection.prepareStatement(
                            "INSERT INTO Accounts (account_id, email, password_hash, role, owner_id, caregiver_id) VALUES (?, ?, ?, ?, ?, ?)")) {
                        account.setInt(1, accountId);
                        account.setString(2, email);
                        account.setString(3, hash);
                        account.setString(4, role);
                        if (role.equals("owner")) {
                            account.setInt(5, profileId); account.setNull(6, java.sql.Types.INTEGER);
                        } else {
                            account.setNull(5, java.sql.Types.INTEGER); account.setInt(6, profileId);
                        }
                        account.executeUpdate();
                    }
                    connection.commit();
                    response.sendRedirect(request.getContextPath() + "/register?created=1");
                    return;
                } catch (SQLException error) {
                    connection.rollback();
                    if ("23000".equals(error.getSQLState())) errors.put("email", "This email is already in use.");
                    else throw error;
                }
            } catch (SQLException error) {
                getServletContext().log("Could not create account.", error);
                response.setStatus(500);
                request.setAttribute("message", "We could not save your account. Please try again.");
            }
        }
        request.setAttribute("errors", errors);
        if (!errors.isEmpty()) response.setStatus(400);
        doGet(request, response);
    }

    // doPost handles one signup at a time so two requests cannot choose the same id.
    // shortcut: this assumes one running app; use database-generated ids before running multiple copies.
    private static int nextId(Connection connection, String table, String idColumn) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement("SELECT MAX(" + idColumn + ") FROM " + table);
             ResultSet rows = query.executeQuery()) {
            rows.next();
            return rows.getInt(1) + 1;
        }
    }
}
