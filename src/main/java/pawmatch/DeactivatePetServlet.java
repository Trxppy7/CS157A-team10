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
import java.sql.SQLException;

@WebServlet("/owner/pets/deactivate")
public class DeactivatePetServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!Web.validCsrf(request)) {
            response.sendError(403);
            return;
        }

        int petId;
        try {
            petId = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException error) {
            response.sendError(400);
            return;
        }

        HttpSession session = request.getSession(false);
        int ownerId = (Integer) session.getAttribute("profileId");

        try (Connection connection = Db.open();
             PreparedStatement update = connection.prepareStatement(
                     "UPDATE Pets SET is_active = FALSE "
                     + "WHERE pet_id = ? AND owner_id = ? AND is_active = TRUE")) {

            update.setInt(1, petId);
            update.setInt(2, ownerId);

            if (update.executeUpdate() == 0) {
                response.sendError(404);
                return;
            }

        } catch (SQLException error) {
            throw new ServletException("Could not deactivate pet.", error);
        }

        response.sendRedirect(request.getContextPath() + "/owner/pets");
    }
}
