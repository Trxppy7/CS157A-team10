package pawmatch;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.Part;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import jakarta.servlet.http.HttpSession;

@WebServlet("/owner/pets/add")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024,
                 maxRequestSize = 6 * 1024 * 1024)
public class AddPetServlet extends HttpServlet {
    private static Path photoDirectory() {
        return Path.of(Db.setting(
                "PET_PHOTO_DIR",
                System.getProperty("user.home") + "/pawmatch-photos"));
    }

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
        request.setAttribute("title", "Add Pet");
        request.getRequestDispatcher("/WEB-INF/views/add-pet.jsp")
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
            response.setStatus(400);
            request.setAttribute("message", "Please enter valid pet information.");
            doGet(request, response);
            return;
        }

        Part photo = request.getPart("photo");
        byte[] photoData = photo == null ? new byte[0] : photo.getInputStream().readAllBytes();

        if (photoData.length > 0
                && (photoExtension(photoData) == null || !validImage(photoData))) {
            response.sendError(400, "Please upload a valid image.");
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
            Path directory = photoDirectory();
            Files.createDirectories(directory);
            Files.write(directory.resolve(photoFilename), photoData);
        }

        try (Connection connection = Db.open();
             PreparedStatement insert = connection.prepareStatement(
                     "INSERT INTO Pets (owner_id, name, species, breed_description, "
                     + "age, size, contact_name, conditions, medication_instructions, "
                     + "feeding_instructions, behavior_notes, vet_contact, photo_filename) "
                     + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

            insert.setInt(1, ownerId);
            insert.setString(2, name);
            insert.setString(3, species);
            insert.setString(4, breedDescription);
            insert.setInt(5, age);
            insert.setString(6, size);
            insert.setString(7, contact);
            insert.setString(8, conditions);
            insert.setString(9, medicationInstructions);
            insert.setString(10, feedingInstructions);
            insert.setString(11, behaviorNotes);
            insert.setString(12, vetContact);
            insert.setString(13, photoFilename);
            insert.executeUpdate();

        } catch (SQLException error) {
            if (photoFilename != null) {
                Files.deleteIfExists(photoDirectory().resolve(photoFilename));
            }
            throw new ServletException("Could not save pet.", error);
        }

        response.sendRedirect(request.getContextPath() + "/owner/pets");
    }
}
