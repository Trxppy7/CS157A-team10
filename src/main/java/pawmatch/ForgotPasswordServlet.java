
package pawmatch;

import jakarta.mail.MessagingException;
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

@WebServlet("/forgot-password")
public class ForgotPasswordServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        request.setAttribute("title", "Forgot Password");
        request.getRequestDispatcher(
                "/WEB-INF/views/forgot-password.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        if (!Web.validCsrf(request)) {
            response.setStatus(403);
            request.setAttribute(
                    "message", "This form expired. Please try again.");
            doGet(request, response);
            return;
        }

        String email = Web.email(request);

        if (!Web.validEmail(email)) {
            response.setStatus(400);
            request.setAttribute(
                    "message", "Enter a valid email address.");
            doGet(request, response);
            return;
        }

        String resetLink = null;

        try (Connection connection = Db.open();
             PreparedStatement query = connection.prepareStatement(
                     "SELECT account_id FROM Accounts WHERE email = ? FOR UPDATE")) {

            connection.setAutoCommit(false);
            query.setString(1, email);

            try {
            try (ResultSet results = query.executeQuery()) {
                if (results.next()) {
                    int accountId = results.getInt("account_id");

                    try (PreparedStatement recent = connection.prepareStatement(
                            "SELECT reset_id FROM PasswordResetTokens " +
                            "WHERE account_id = ? " +
                            "AND created_at > DATE_SUB(UTC_TIMESTAMP(), INTERVAL 60 SECOND) " +
                            "LIMIT 1")) {
                        recent.setInt(1, accountId);
                        try (ResultSet recentResults = recent.executeQuery()) {
                            if (recentResults.next()) {
                                connection.rollback();
                                request.setAttribute("message",
                                        "If an account exists for that email, " +
                                        "password reset instructions will be sent.");
                                doGet(request, response);
                                return;
                            }
                        }
                    }

                    String token = ResetTokens.generate();
                    String tokenHash = ResetTokens.hash(token);

                    try (PreparedStatement insert =
                                 connection.prepareStatement(
                         "INSERT INTO PasswordResetTokens " +
                         "(account_id, token_hash, expires_at) " +
                         "VALUES (?, ?, DATE_ADD(UTC_TIMESTAMP(), " +
                         "INTERVAL 30 MINUTE))")) {

                        insert.setInt(1, accountId);
                        insert.setString(2, tokenHash);
                        insert.executeUpdate();
                    }

                    String baseUrl = Db.setting("APP_BASE_URL", "").strip();
                    if (!baseUrl.matches("https?://[^\\s]+")) {
                        throw new SQLException("APP_BASE_URL is invalid.");
                    }

                    resetLink = baseUrl.replaceAll("/+$", "")
                            + "/reset-password?token=" + token;

                    connection.commit();
                }
            }
            } catch (SQLException error) {
                if (!connection.getAutoCommit()) {
                    connection.rollback();
                }
                throw error;
            }

        } catch (SQLException error) {
            getServletContext().log(
                    "Could not create password reset request.", error);

            response.setStatus(503);
            request.setAttribute("message",
                    "Password recovery is temporarily unavailable.");
            doGet(request, response);
            return;
        }

        if (resetLink != null) {
            try {
                PasswordResetEmail.send(email, resetLink);
            } catch (MessagingException error) {
                getServletContext().log("Could not send password reset email.", error);
                response.setStatus(503);
                request.setAttribute("message",
                        "Password recovery is temporarily unavailable.");
                doGet(request, response);
                return;
            }
        }

        request.setAttribute("message",
                "If an account exists for that email, " +
                "password reset instructions will be sent.");

        doGet(request, response);
    }
}

