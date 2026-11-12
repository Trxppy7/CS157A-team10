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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Lists the care requests the owner has created, newest first.
@WebServlet("/owner/requests")
public class CareRequestsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        int ownerId = (Integer) session.getAttribute("profileId");

        List<Map<String, Object>> requests = new ArrayList<>();
        try (Connection connection = Db.open();
             PreparedStatement query = connection.prepareStatement(
                     "SELECT r.request_id, r.location, r.status, r.created_at, "
                     + "p.name AS pet_name, s.category AS service, "
                     + "COUNT(v.request_visit_id) AS visits "
                     + "FROM CareRequests r, Pets p, Services s, RequestVisits v "
                     + "WHERE p.pet_id = r.pet_id "
                     + "AND s.service_id = r.service_id "
                     + "AND v.request_id = r.request_id "
                     + "AND p.owner_id = ? "
                     + "GROUP BY r.request_id, r.location, r.status, r.created_at, p.name, s.category "
                     + "ORDER BY r.created_at DESC, r.request_id DESC")) {
            query.setInt(1, ownerId);
            try (ResultSet rows = query.executeQuery()) {
                while (rows.next()) {
                    Map<String, Object> req = new LinkedHashMap<>();
                    req.put("id", rows.getInt("request_id"));
                    req.put("pet", rows.getString("pet_name"));
                    req.put("service", rows.getString("service"));
                    req.put("location", rows.getString("location"));
                    req.put("status", rows.getString("status"));
                    req.put("created", rows.getString("created_at"));
                    req.put("visits", rows.getInt("visits"));
                    requests.add(req);
                }
            }
        } catch (SQLException error) {
            throw new ServletException("Could not load care requests.", error);
        }

        request.setAttribute("requests", requests);
        request.setAttribute("title", "Care requests");
        request.getRequestDispatcher("/WEB-INF/views/care-requests.jsp").forward(request, response);
    }
}
