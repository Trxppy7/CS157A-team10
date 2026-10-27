
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

@WebServlet("/reset-password")
public class ResetPasswordServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Referrer-Policy", "no-referrer");

        String token = Web.text(request, "token");
        request.setAttribute("title", "Reset Password");

        if (validToken(token)) {
            try (Connection connection = Db.open();
                 PreparedStatement query = connection.prepareStatement(
                         "SELECT reset_id FROM PasswordResetTokens " +
                         "WHERE token_hash = ? AND used_at IS NULL " +
                         "AND expires_at > UTC_TIMESTAMP()")) {

                query.setString(1, ResetTokens.hash(token));

                try (ResultSet results = query.executeQuery()) {
                    if (results.next()) {
                        request.setAttribute("validToken", true);
                        request.setAttribute("token", token);
                    } else {
                        request.setAttribute("message",
                                "This password reset link is invalid or expired.");
                    }
                }

            } catch (SQLException error) {
                getServletContext().log("Could not validate reset token.", error);
                response.setStatus(503);
                request.setAttribute("message",
                        "Password recovery is temporarily unavailable.");
            }
        } else {
            request.setAttribute("message",
                    "This password reset link is invalid or expired.");
        }

        request.getRequestDispatcher("/WEB-INF/views/reset-password.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        if (!Web.validCsrf(request)) {
            response.sendError(403);
            return;
        }

        String token = Web.text(request, "token");
        String password = request.getParameter("password");
        String confirmation = request.getParameter("confirmPassword");

        if (!validToken(token) || password == null ||
                password.length() < 8 || password.length() > 128 ||
                !password.equals(confirmation)) {

            response.setStatus(400);
            request.setAttribute("message",
                    "Enter matching passwords containing 8 to 128 characters.");
            doGet(request, response);
            return;
        }

        try (Connection connection = Db.open()) {
            connection.setAutoCommit(false);

            try {
                int accountId = -1;

                try (PreparedStatement query = connection.prepareStatement(
                        "SELECT account_id FROM PasswordResetTokens " +
                        "WHERE token_hash = ? AND used_at IS NULL " +
                        "AND expires_at > UTC_TIMESTAMP() FOR UPDATE")) {

                    query.setString(1, ResetTokens.hash(token));

                    try (ResultSet results = query.executeQuery()) {
                        if (results.next()) {
                            accountId = results.getInt("account_id");
                        }
                    }
                }

                if (accountId == -1) {
                    connection.rollback();
                    response.setStatus(400);
                    request.setAttribute("message",
                            "This password reset link is invalid or expired.");
                    doGet(request, response);
                    return;
                }

                try (PreparedStatement update = connection.prepareStatement(
                        "UPDATE Accounts SET password_hash = ? WHERE account_id = ?")) {

                    update.setString(1, Passwords.hash(password));
                    update.setInt(2, accountId);
                    update.executeUpdate();
                }

                try (PreparedStatement update = connection.prepareStatement(
                        "UPDATE PasswordResetTokens SET used_at = UTC_TIMESTAMP() " +
                        "WHERE account_id = ? AND used_at IS NULL")) {

                    update.setInt(1, accountId);
                    update.executeUpdate();
                }

                connection.commit();

                response.sendRedirect(request.getContextPath() + "/login?reset=1");
                return;

            } catch (SQLException error) {
                connection.rollback();
                throw error;
            }

        } catch (SQLException error) {
            getServletContext().log("Could not reset password.", error);
            response.setStatus(503);
            request.setAttribute("message",
                    "Password recovery is temporarily unavailable.");
            doGet(request, response);
        }
    }

    private boolean validToken(String token) {
        return token != null &&
                token.matches("[A-Za-z0-9_-]{43}");
    }
}
