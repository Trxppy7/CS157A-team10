package pawmatch;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/owner/pets/photo")
public class PetPhotoServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        int ownerId = (Integer) session.getAttribute("profileId");

        int petId;
        try {
            petId = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException error) {
            response.sendError(400);
            return;
        }

        String filename;

        try (Connection connection = Db.open();
             PreparedStatement query = connection.prepareStatement(
                     "SELECT photo_filename FROM Pets WHERE pet_id = ? AND owner_id = ?")) {

            query.setInt(1, petId);
            query.setInt(2, ownerId);

            try (ResultSet rows = query.executeQuery()) {
                if (!rows.next()) {
                    response.sendError(404);
                    return;
                }
                filename = rows.getString("photo_filename");
            }
        } catch (SQLException error) {
            throw new ServletException("Could not load pet photo.", error);
        }

        if (filename == null || !filename.matches(
                "[0-9a-fA-F-]{36}\\.(jpg|png|webp)")) {
            response.sendError(404);
            return;
        }

        Path directory = Path.of(Db.setting(
                "PET_PHOTO_DIR",
                System.getProperty("user.home") + "/pawmatch-photos"));
        Path photo = directory.resolve(filename);

        if (!Files.isRegularFile(photo)) {
            response.sendError(404);
            return;
        }

        if (filename.endsWith(".png")) {
            response.setContentType("image/png");
        } else if (filename.endsWith(".webp")) {
            response.setContentType("image/webp");
        } else {
            response.setContentType("image/jpeg");
        }

        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Cache-Control", "private, no-store");
        Files.copy(photo, response.getOutputStream());
    }
}
