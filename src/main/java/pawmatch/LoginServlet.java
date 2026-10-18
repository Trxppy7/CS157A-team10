package pawmatch;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final String DUMMY_HASH = Passwords.hash("unused-password-for-timing");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("accountId") != null) {
            response.sendRedirect(request.getContextPath() + "/" + session.getAttribute("role") + "/home");
            return;
        }
        if (session != null && session.getAttribute("registeredEmail") != null) {
            request.setAttribute("email", session.getAttribute("registeredEmail"));
            session.removeAttribute("registeredEmail");
        }
        request.setAttribute("title", "Log in");
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!Web.validCsrf(request)) {
            response.setStatus(403);
            request.setAttribute("message", "This form expired. Please try again.");
            doGet(request, response);
            return;
        }
        String email = Web.email(request);
        String password = request.getParameter("password");
        if (password != null && password.length() <= 128 && email.length() <= 100) {
            try (Connection connection = Db.open(); PreparedStatement query = connection.prepareStatement(
                    "SELECT account_id, password_hash, role, owner_id, caregiver_id FROM Accounts WHERE email = ?")) {
                query.setString(1, email);
                try (ResultSet rows = query.executeQuery()) {
                    boolean found = rows.next();
                    boolean matches = Passwords.matches(password, found ? rows.getString("password_hash") : DUMMY_HASH);
                    if (found && matches) {
                        request.getSession().invalidate();
                        HttpSession session = request.getSession(true);
                        session.setAttribute("accountId", rows.getInt("account_id"));
                        String role = rows.getString("role");
                        session.setAttribute("role", role);
                        session.setAttribute("profileId", role.equals("owner") ? rows.getInt("owner_id") : rows.getInt("caregiver_id"));
                        response.sendRedirect(request.getContextPath() + "/" + role + "/home");
                        return;
                    }
                }
            } catch (SQLException error) {
                getServletContext().log("Could not log in.", error);
                response.setStatus(503);
                request.setAttribute("message", "We could not log you in right now. Please try again.");
                doGet(request, response);
                return;
            }
        }
        response.setStatus(400);
        request.setAttribute("message", "That email and password don't match an account. Check both and try again.");
        doGet(request, response);
    }
}
