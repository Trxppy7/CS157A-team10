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

@WebServlet({"/owner/home", "/caregiver/home", "/admin/home"})
public class HomeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        try (Connection connection = Db.open()) {
            String email;
            int ownerId = 0, caregiverId = 0;
            boolean hasOwner = false, hasCaregiver = false;
            try (PreparedStatement query = connection.prepareStatement(
                    "SELECT email, owner_id, caregiver_id FROM Accounts WHERE account_id = ?")) {
                query.setInt(1, (Integer) session.getAttribute("accountId"));
                try (ResultSet rows = query.executeQuery()) {
                    if (!rows.next()) {
                        session.invalidate(); response.sendRedirect(request.getContextPath() + "/login"); return;
                    }
                    email = rows.getString("email");
                    ownerId = rows.getInt("owner_id"); hasOwner = !rows.wasNull();
                    caregiverId = rows.getInt("caregiver_id"); hasCaregiver = !rows.wasNull();
                }
            }
            String name = "Administrator";
            String phone = "";
            if (hasOwner) {
                try (PreparedStatement query = connection.prepareStatement(
                        "SELECT name, phone_number FROM Owners WHERE owner_id = ?")) {
                    query.setInt(1, ownerId);
                    try (ResultSet rows = query.executeQuery()) {
                        if (rows.next()) { name = rows.getString("name"); phone = rows.getString("phone_number"); }
                    }
                }
            } else if (hasCaregiver) {
                try (PreparedStatement query = connection.prepareStatement(
                        "SELECT name, phone_number FROM Caregivers WHERE caregiver_id = ?")) {
                    query.setInt(1, caregiverId);
                    try (ResultSet rows = query.executeQuery()) {
                        if (rows.next()) { name = rows.getString("name"); phone = rows.getString("phone_number"); }
                    }
                }
            }
            if (phone == null) phone = "";
            request.setAttribute("name", name);
            request.setAttribute("email", email);
            request.setAttribute("phone", phone);
        } catch (SQLException error) { throw new ServletException("Could not load account.", error); }
        request.setAttribute("title", "Your account");
        request.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(request, response);
    }
}
