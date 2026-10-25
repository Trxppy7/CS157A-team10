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
        try (Connection connection = Db.open(); PreparedStatement query = connection.prepareStatement(
                "SELECT a.email, COALESCE(o.name, c.name, 'Administrator') AS name, COALESCE(o.phone_number, c.phone_number, '') AS phone "
                + "FROM Accounts a LEFT JOIN Owners o ON a.owner_id = o.owner_id LEFT JOIN Caregivers c ON a.caregiver_id = c.caregiver_id WHERE a.account_id = ?")) {
            query.setInt(1, (Integer) session.getAttribute("accountId"));
            try (ResultSet rows = query.executeQuery()) {
                if (!rows.next()) {
                    session.invalidate(); response.sendRedirect(request.getContextPath() + "/login"); return;
                }
                request.setAttribute("name", rows.getString("name"));
                request.setAttribute("email", rows.getString("email"));
                request.setAttribute("phone", rows.getString("phone"));
            }
        } catch (SQLException error) { throw new ServletException("Could not load account.", error); }
        request.setAttribute("title", "Your account");
        request.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(request, response);
    }
}
