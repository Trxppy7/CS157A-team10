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
import jakarta.servlet.http.HttpSession;

@WebServlet("/owner/edit-profile")
public class EditOwnerProfileServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        int ownerId = (Integer) session.getAttribute("profileId");

        try (Connection connection = Db.open();
             PreparedStatement query = connection.prepareStatement(
                     "SELECT name, email, phone_number FROM Owners WHERE owner_id = ?")) {

            query.setInt(1, ownerId);

            try (ResultSet rows = query.executeQuery()) {
                if (!rows.next()) {
                    response.sendError(404);
                    return;
                }

                request.setAttribute("name", rows.getString("name"));
                request.setAttribute("email", rows.getString("email"));
                request.setAttribute("phone", rows.getString("phone_number"));
            }
        } catch (SQLException error) {
            throw new ServletException("Could not load owner profile.", error);
        }

        request.setAttribute("title", "Edit Profile");
        request.getRequestDispatcher("/WEB-INF/views/edit-owner-profile.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        if (!Web.validCsrf(request)) {
            response.sendError(403);
            return;
        }

        String name = Web.text(request, "name");
        String email = Web.email(request);
        String phone = Web.text(request, "phone");

        if (name.isBlank() || name.length() > 100
                || !Web.validEmail(email) || !Web.validPhone(phone)) {
            response.setStatus(400);
            request.setAttribute("message", "Please enter a valid name, email, and phone number.");
            doGet(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        int ownerId = (Integer) session.getAttribute("profileId");
        int accountId = (Integer) session.getAttribute("accountId");

        try (Connection connection = Db.open()) {
            connection.setAutoCommit(false);

            try {
                try (PreparedStatement check = connection.prepareStatement(
                        "SELECT email FROM Accounts WHERE email = ? AND account_id <> ? "
                        + "UNION SELECT email FROM Owners WHERE email = ? AND owner_id <> ? "
                        + "UNION SELECT email FROM Caregivers WHERE email = ?")) {
                    check.setString(1, email);
                    check.setInt(2, accountId);
                    check.setString(3, email);
                    check.setInt(4, ownerId);
                    check.setString(5, email);

                    try (ResultSet rows = check.executeQuery()) {
                        if (rows.next()) {
                            connection.rollback();
                            response.setStatus(400);
                            request.setAttribute("message", "This email is already in use.");
                            request.setAttribute("name", name);
                            request.setAttribute("email", email);
                            request.setAttribute("phone", phone);
                            request.setAttribute("title", "Edit Profile");
                            request.getRequestDispatcher("/WEB-INF/views/edit-owner-profile.jsp")
                                    .forward(request, response);
                            return;
                        }
                    }
                }

                try (PreparedStatement update = connection.prepareStatement(
                        "UPDATE Owners SET name = ?, email = ?, phone_number = ? WHERE owner_id = ?")) {
                    update.setString(1, name);
                    update.setString(2, email);
                    update.setString(3, phone);
                    update.setInt(4, ownerId);
                    if (update.executeUpdate() != 1) {
                        throw new SQLException("Owner profile not found.");
                    }
                }

                try (PreparedStatement update = connection.prepareStatement(
                        "UPDATE Accounts SET email = ? WHERE account_id = ? AND owner_id = ?")) {
                    update.setString(1, email);
                    update.setInt(2, accountId);
                    update.setInt(3, ownerId);
                    if (update.executeUpdate() != 1) {
                        throw new SQLException("Owner account not found.");
                    }
                }

                connection.commit();
            } catch (SQLException error) {
                connection.rollback();
                if ("23000".equals(error.getSQLState())) {
                    response.setStatus(400);
                    request.setAttribute("message", "This email is already in use.");
                    request.setAttribute("name", name);
                    request.setAttribute("email", email);
                    request.setAttribute("phone", phone);
                    request.setAttribute("title", "Edit Profile");
                    request.getRequestDispatcher("/WEB-INF/views/edit-owner-profile.jsp")
                            .forward(request, response);
                    return;
                }
                throw error;
            }
        } catch (SQLException error) {
            throw new ServletException("Could not update owner profile.", error);
        }

        response.sendRedirect(request.getContextPath() + "/owner/home?updated=1");
    }
}
