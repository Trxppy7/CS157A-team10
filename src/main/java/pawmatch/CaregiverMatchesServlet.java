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

@WebServlet("/owner/matches")
public class CaregiverMatchesServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int requestId;
        try {
            requestId = Integer.parseInt(Web.text(request, "requestId"));
            if (requestId < 1) throw new NumberFormatException();
        } catch (NumberFormatException error) {
            response.sendError(400, "Choose a care request.");
            return;
        }
        int ownerId = (Integer) request.getSession(false).getAttribute("profileId");
        try (Connection connection = Db.open();
             PreparedStatement query = connection.prepareStatement(
                     "SELECT r.location, r.status, p.name, p.species, s.category "
                     + "FROM CareRequests r, Pets p, Services s WHERE r.request_id = ? "
                     + "AND r.pet_id = p.pet_id AND r.service_id = s.service_id AND p.owner_id = ?")) {
            query.setInt(1, requestId);
            query.setInt(2, ownerId);
            try (ResultSet rows = query.executeQuery()) {
                if (!rows.next()) {
                    response.sendError(404);
                    return;
                }
                request.setAttribute("pet", rows.getString("name"));
                request.setAttribute("species", rows.getString("species"));
                request.setAttribute("service", rows.getString("category"));
                request.setAttribute("location", rows.getString("location"));
                request.setAttribute("status", rows.getString("status"));
            }
            request.setAttribute("matches", CaregiverMatches.find(connection, requestId, 0));
        } catch (SQLException error) {
            throw new ServletException("Could not load caregiver matches.", error);
        }
        request.setAttribute("requestId", requestId);
        request.setAttribute("title", "Caregiver matches");
        request.getRequestDispatcher("/WEB-INF/views/caregiver-matches.jsp").forward(request, response);
    }
}
