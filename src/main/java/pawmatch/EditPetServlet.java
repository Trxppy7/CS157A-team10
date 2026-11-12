package pawmatch;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.Part;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import javax.imageio.ImageIO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/owner/pets/edit")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024,
                 maxRequestSize = 6 * 1024 * 1024)
public class EditPetServlet extends HttpServlet {
    private static String photoExtension(byte[] data) {
        if (data.length >= 3
                && (data[0] & 255) == 255
                && (data[1] & 255) == 216
                && (data[2] & 255) == 255) {
            return ".jpg";
        }

        if (data.length >= 8
                && (data[0] & 255) == 137
                && data[1] == 80
                && data[2] == 78
                && data[3] == 71
                && data[4] == 13
                && data[5] == 10
                && data[6] == 26
                && data[7] == 10) {
            return ".png";
        }

        return null;
    }

    private static boolean validImage(byte[] data) {
        try {
            java.awt.image.BufferedImage image =
                    ImageIO.read(new ByteArrayInputStream(data));
            return image != null
                    && image.getWidth() <= 4000
                    && image.getHeight() <= 4000;
        } catch (IOException error) {
            return false;
        }
    }

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

        Map<String, Object> pet = new LinkedHashMap<>();

        try (Connection connection = Db.open();
             PreparedStatement query = connection.prepareStatement(
                     "SELECT pet_id, name, species, breed_description, age, size, "
                     + "contact_name, conditions, medication_instructions, "
                     + "feeding_instructions, behavior_notes, vet_contact "
                     + "FROM Pets WHERE pet_id = ? AND owner_id = ?")) {

            query.setInt(1, petId);
            query.setInt(2, ownerId);

            try (ResultSet rows = query.executeQuery()) {
                if (!rows.next()) {
                    response.sendError(404);
                    return;
                }

                pet.put("id", rows.getInt("pet_id"));
                pet.put("name", rows.getString("name"));
                pet.put("species", rows.getString("species"));
                pet.put("breedDescription", rows.getString("breed_description"));
                pet.put("age", rows.getObject("age"));
                pet.put("size", rows.getString("size"));
                pet.put("contact", rows.getString("contact_name"));
                pet.put("conditions", rows.getString("conditions"));
                pet.put("medicationInstructions", rows.getString("medication_instructions"));
                pet.put("feedingInstructions", rows.getString("feeding_instructions"));
                pet.put("behaviorNotes", rows.getString("behavior_notes"));
                pet.put("vetContact", rows.getString("vet_contact"));
            }
        } catch (SQLException error) {
            throw new ServletException("Could not load pet.", error);
        }

        request.setAttribute("pet", pet);
        request.setAttribute("title", "Edit Pet");
        request.getRequestDispatcher("/WEB-INF/views/edit-pet.jsp")
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

        int petId;
        try {
            petId = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException error) {
            response.sendError(400);
            return;
        }

        String name = Web.text(request, "name");
        String species = Web.text(request, "species");
        String breedDescription = Web.text(request, "breed_description");
        String ageText = Web.text(request, "age");
        String size = Web.text(request, "size");
        String contact = Web.text(request, "contact");
        String conditions = Web.text(request, "conditions");
        String medicationInstructions = Web.text(request, "medication_instructions");
        String feedingInstructions = Web.text(request, "feeding_instructions");
        String behaviorNotes = Web.text(request, "behavior_notes");
        String vetContact = Web.text(request, "vet_contact");

        int age;
        try {
            age = Integer.parseInt(ageText);
        } catch (NumberFormatException error) {
            age = -1;
        }

        if (name.isBlank() || name.length() > 50
                || species.isBlank() || species.length() > 30
                || breedDescription.isBlank() || breedDescription.length() > 100
                || age < 0 || age > 100
                || (!size.equals("Small") && !size.equals("Medium")
                    && !size.equals("Large"))
                || contact.length() > 100 || conditions.length() > 200
                || medicationInstructions.length() > 500
                || feedingInstructions.length() > 500
                || behaviorNotes.length() > 500
                || vetContact.length() > 200) {
            response.sendError(400, "Please enter valid pet information.");
            return;
        }

        Part photo = request.getPart("photo");
        byte[] photoData = photo == null ? new byte[0]
                : photo.getInputStream().readAllBytes();

        if (photoData.length > 0
                && (photoExtension(photoData) == null || !validImage(photoData))) {
            response.sendError(400, "Please upload a valid JPEG or PNG image.");
            return;
        }

        HttpSession session = request.getSession(false);
        int ownerId = (Integer) session.getAttribute("profileId");

        String photoFilename = null;
        if (photoData.length > 0) {
            photoFilename = UUID.randomUUID().toString()
                    + photoExtension(photoData);
        }

        if (photoFilename != null) {
            Path directory = Path.of(Db.setting(
                    "PET_PHOTO_DIR",
                    System.getProperty("user.home") + "/pawmatch-photos"));
            Files.createDirectories(directory);
            Files.write(directory.resolve(photoFilename), photoData);
        }

        try (Connection connection = Db.open();
             PreparedStatement update = connection.prepareStatement(
                     "UPDATE Pets SET name = ?, species = ?, "
                     + "breed_description = ?, age = ?, size = ?, "
                     + "contact_name = ?, conditions = ?, "
                     + "medication_instructions = ?, feeding_instructions = ?, "
                     + "behavior_notes = ?, vet_contact = ?, "
                     + "photo_filename = COALESCE(?, photo_filename) "
                     + "WHERE pet_id = ? AND owner_id = ?")) {

            update.setString(1, name);
            update.setString(2, species);
            update.setString(3, breedDescription);
            update.setInt(4, age);
            update.setString(5, size);
            update.setString(6, contact);
            update.setString(7, conditions);
            update.setString(8, medicationInstructions);
            update.setString(9, feedingInstructions);
            update.setString(10, behaviorNotes);
            update.setString(11, vetContact);
            update.setString(12, photoFilename);
            update.setInt(13, petId);
            update.setInt(14, ownerId);

            if (update.executeUpdate() == 0) {
                if (photoFilename != null) {
                    Path directory = Path.of(Db.setting(
                            "PET_PHOTO_DIR",
                            System.getProperty("user.home") + "/pawmatch-photos"));
                    Files.deleteIfExists(directory.resolve(photoFilename));
                }
                response.sendError(404);
                return;
            }

        } catch (SQLException error) {
            if (photoFilename != null) {
                Path directory = Path.of(Db.setting(
                        "PET_PHOTO_DIR",
                        System.getProperty("user.home") + "/pawmatch-photos"));
                Files.deleteIfExists(directory.resolve(photoFilename));
            }
            throw new ServletException("Could not update pet.", error);
        }

        response.sendRedirect(request.getContextPath() + "/owner/pets");
    }
}
