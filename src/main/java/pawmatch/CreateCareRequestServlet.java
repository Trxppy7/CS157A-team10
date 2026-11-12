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

// Lets an owner request care for one of their active pets.
@WebServlet("/owner/requests/new")
public class CreateCareRequestServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        loadOptions(request, (Integer) request.getSession(false).getAttribute("profileId"));
        request.setAttribute("title", "Request care");
        request.getRequestDispatcher("/WEB-INF/views/create-care-request.jsp").forward(request, response);
    }

    @Override
    protected synchronized void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!Web.validCsrf(request)) {
            response.setStatus(403);
            request.setAttribute("message", "This form expired. Please try again.");
            doGet(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        int ownerId = (Integer) session.getAttribute("profileId");

        String petRaw = Web.text(request, "petId");
        String serviceRaw = Web.text(request, "serviceId");
        String location = Web.text(request, "location");
        String instructions = Web.text(request, "instructions");
        String[] visitDates = request.getParameterValues("visitDate");
        String[] visitTimes = request.getParameterValues("visitTime");

        Map<String, String> errors = new LinkedHashMap<>();

        int petId = parsePositive(petRaw);
        int serviceId = parsePositive(serviceRaw);
        if (petId == 0) errors.put("petId", "Choose one of your active pets.");
        if (serviceId == 0) errors.put("serviceId", "Choose a service.");
        if (location.isBlank() || location.length() > 200) errors.put("location", "Enter a location (up to 200 characters).");
        if (instructions.length() > 500) errors.put("instructions", "Use 500 characters or fewer.");

        // Collect the visit dates/times the owner entered.
        List<String[]> visits = new ArrayList<>();
        if (visitDates != null) {
            for (int i = 0; i < visitDates.length; i++) {
                String date = visitDates[i] == null ? "" : visitDates[i].trim();
                String time = (visitTimes != null && i < visitTimes.length && visitTimes[i] != null)
                        ? visitTimes[i].trim() : "";
                if (date.isBlank() && time.isBlank()) continue;
                if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    errors.put("visits", "Enter each visit date as YYYY-MM-DD.");
                    break;
                }
                if (!time.isBlank() && !time.matches("\\d{2}:\\d{2}")) {
                    errors.put("visits", "Enter each visit time as HH:MM.");
                    break;
                }
                visits.add(new String[] {date, time.isBlank() ? null : time});
            }
        }
        if (!errors.containsKey("visits") && visits.isEmpty()) {
            errors.put("visits", "Add at least one visit date.");
        }

        if (errors.isEmpty()) {
            try (Connection connection = Db.open()) {
                connection.setAutoCommit(false);
                try {
                    // Confirm the pet is the owner's and still active.
                    boolean ok = false;
                    try (PreparedStatement check = connection.prepareStatement(
                            "SELECT pet_id FROM Pets WHERE pet_id = ? AND owner_id = ? AND is_active = 1")) {
                        check.setInt(1, petId);
                        check.setInt(2, ownerId);
                        try (ResultSet rows = check.executeQuery()) {
                            ok = rows.next();
                        }
                    }
                    if (!ok) {
                        connection.rollback();
                        errors.put("petId", "Choose one of your active pets.");
                        throw new FormException();
                    }

                    int requestId = Db.nextId(connection, "CareRequests", "request_id");
                    try (PreparedStatement insert = connection.prepareStatement(
                            "INSERT INTO CareRequests (request_id, service_id, pet_id, location, instructions, status, created_at) "
                            + "VALUES (?, ?, ?, ?, ?, 'open', ?)")) {
                        insert.setInt(1, requestId);
                        insert.setInt(2, serviceId);
                        insert.setInt(3, petId);
                        insert.setString(4, location);
                        insert.setString(5, instructions.isBlank() ? null : instructions);
                        insert.setString(6, java.time.LocalDateTime.now()
                                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                        insert.executeUpdate();
                    }

                    int visitId = Db.nextId(connection, "RequestVisits", "request_visit_id");
                    try (PreparedStatement insert = connection.prepareStatement(
                            "INSERT INTO RequestVisits (request_visit_id, request_id, visit_date, visit_time) "
                            + "VALUES (?, ?, ?, ?)")) {
                        for (String[] visit : visits) {
                            insert.setInt(1, visitId++);
                            insert.setInt(2, requestId);
                            insert.setString(3, visit[0]);
                            insert.setString(4, visit[1]);
                            insert.addBatch();
                        }
                        insert.executeBatch();
                    }
                    connection.commit();
                    response.sendRedirect(request.getContextPath() + "/owner/requests?created=1");
                    return;
                } catch (FormException shown) {
                    // Validation failure detected inside the transaction; fall through to re-render.
                } catch (SQLException error) {
                    connection.rollback();
                    throw error;
                } finally {
                    connection.setAutoCommit(true);
                }
            } catch (SQLException error) {
                throw new ServletException("Could not save the care request.", error);
            }
        }

        request.setAttribute("errors", errors);
        response.setStatus(400);
        loadOptions(request, ownerId);
        request.setAttribute("title", "Request care");
        request.getRequestDispatcher("/WEB-INF/views/create-care-request.jsp").forward(request, response);
    }

    // Loads the owner's active pets and the service list for the form's dropdowns.
    private void loadOptions(HttpServletRequest request, int ownerId) throws ServletException {
        List<Map<String, Object>> pets = new ArrayList<>();
        List<Map<String, Object>> services = new ArrayList<>();
        try (Connection connection = Db.open()) {
            try (PreparedStatement query = connection.prepareStatement(
                    "SELECT pet_id, name, species FROM Pets WHERE owner_id = ? AND is_active = 1 ORDER BY name")) {
                query.setInt(1, ownerId);
                try (ResultSet rows = query.executeQuery()) {
                    while (rows.next()) {
                        Map<String, Object> pet = new LinkedHashMap<>();
                        pet.put("id", rows.getInt("pet_id"));
                        pet.put("name", rows.getString("name"));
                        pet.put("species", rows.getString("species"));
                        pets.add(pet);
                    }
                }
            }
            try (PreparedStatement query = connection.prepareStatement(
                    "SELECT service_id, category FROM Services ORDER BY category");
                 ResultSet rows = query.executeQuery()) {
                while (rows.next()) {
                    Map<String, Object> service = new LinkedHashMap<>();
                    service.put("id", rows.getInt("service_id"));
                    service.put("category", rows.getString("category"));
                    services.add(service);
                }
            }
        } catch (SQLException error) {
            throw new ServletException("Could not load the request form.", error);
        }
        request.setAttribute("pets", pets);
        request.setAttribute("services", services);
    }

    private static int parsePositive(String value) {
        try {
            int n = Integer.parseInt(value);
            return n > 0 ? n : 0;
        } catch (NumberFormatException error) {
            return 0;
        }
    }

    // Signals a validation problem found mid-transaction so we roll back and re-render.
    private static class FormException extends RuntimeException {}
}
