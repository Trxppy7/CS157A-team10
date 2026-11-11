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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/owner/pets")
public class PetsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        int ownerId = (Integer) session.getAttribute("profileId");

        List<Map<String, Object>> pets = new ArrayList<>();

        try (Connection connection = Db.open();
             PreparedStatement query = connection.prepareStatement(
                     "SELECT pet_id, name, species, breed_description, age, size, "
                     + "contact_name, conditions, medication_instructions, "
                     + "feeding_instructions, behavior_notes, vet_contact, "
                     + "photo_filename, is_active "
                     + "FROM Pets WHERE owner_id = ? ORDER BY pet_id")) {

            query.setInt(1, ownerId);

            try (ResultSet rows = query.executeQuery()) {
                while (rows.next()) {
                    Map<String, Object> pet = new LinkedHashMap<>();
                    pet.put("id", rows.getInt("pet_id"));
                    pet.put("active", rows.getBoolean("is_active"));
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
                    pet.put("photoFilename", rows.getString("photo_filename"));
                    pets.add(pet);
                }
            }
        } catch (SQLException error) {
            throw new ServletException("Could not load pets.", error);
        }

        request.setAttribute("pets", pets);
        request.setAttribute("title", "My Pets");
        request.getRequestDispatcher("/WEB-INF/views/pets.jsp")
                .forward(request, response);
    }
}
