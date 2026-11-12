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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/owner/bookings")
public class BookingsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Map<String, Object>> bookings = new ArrayList<>();
        try (Connection connection = Db.open();
             PreparedStatement query = connection.prepareStatement(
                     "SELECT b.booking_id, b.status, b.total_price, c.name AS caregiver, p.name AS pet, "
                     + "s.category, r.location FROM Bookings b, Caregivers c, CareRequests r, Pets p, Services s "
                     + "WHERE b.caregiver_id = c.caregiver_id AND b.request_id = r.request_id "
                     + "AND r.pet_id = p.pet_id AND r.service_id = s.service_id AND p.owner_id = ? "
                     + "ORDER BY b.booking_id DESC")) {
            query.setInt(1, (Integer) request.getSession(false).getAttribute("profileId"));
            try (ResultSet rows = query.executeQuery()) {
                while (rows.next()) {
                    Map<String, Object> booking = new LinkedHashMap<>();
                    booking.put("id", rows.getInt("booking_id"));
                    booking.put("status", rows.getString("status"));
                    booking.put("total", rows.getBigDecimal("total_price"));
                    booking.put("caregiver", rows.getString("caregiver"));
                    booking.put("pet", rows.getString("pet"));
                    booking.put("service", rows.getString("category"));
                    booking.put("location", rows.getString("location"));
                    List<String> visits = new ArrayList<>();
                    try (PreparedStatement schedule = connection.prepareStatement(
                            "SELECT visit_date, visit_time FROM Visits WHERE booking_id = ? ORDER BY visit_date, visit_time")) {
                        schedule.setInt(1, rows.getInt("booking_id"));
                        try (ResultSet times = schedule.executeQuery()) {
                            while (times.next()) visits.add(times.getString(1)
                                    + (times.getString(2) == null ? "" : " " + times.getString(2)));
                        }
                    }
                    booking.put("visits", visits);
                    bookings.add(booking);
                }
            }
        } catch (SQLException error) {
            throw new ServletException("Could not load bookings.", error);
        }
        request.setAttribute("bookings", bookings);
        request.setAttribute("title", "My bookings");
        request.getRequestDispatcher("/WEB-INF/views/bookings.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!Web.validCsrf(request)) {
            response.sendError(403, "This form expired. Please try again.");
            return;
        }
        int requestId, caregiverId;
        try {
            requestId = Integer.parseInt(Web.text(request, "requestId"));
            caregiverId = Integer.parseInt(Web.text(request, "caregiverId"));
            if (requestId < 1 || caregiverId < 1) throw new NumberFormatException();
        } catch (NumberFormatException error) {
            response.sendError(400, "Choose a request and caregiver.");
            return;
        }
        int ownerId = (Integer) request.getSession(false).getAttribute("profileId");
        try (Connection connection = Db.open()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement query = connection.prepareStatement(
                        "SELECT r.status FROM CareRequests r, Pets p WHERE r.request_id = ? "
                        + "AND r.pet_id = p.pet_id AND p.owner_id = ? FOR UPDATE")) {
                    query.setInt(1, requestId);
                    query.setInt(2, ownerId);
                    try (ResultSet rows = query.executeQuery()) {
                        if (!rows.next()) {
                            connection.rollback();
                            response.sendError(404);
                            return;
                        }
                        if (!"open".equals(rows.getString("status"))) {
                            connection.rollback();
                            response.sendRedirect(request.getContextPath() + "/owner/matches?requestId=" + requestId + "&unavailable=1");
                            return;
                        }
                    }
                }
                try (PreparedStatement query = connection.prepareStatement(
                        "SELECT caregiver_id FROM Caregivers WHERE caregiver_id = ? FOR UPDATE")) {
                    query.setInt(1, caregiverId);
                    try (ResultSet rows = query.executeQuery()) {
                        if (!rows.next()) {
                            connection.rollback();
                            response.sendError(404);
                            return;
                        }
                    }
                }
                List<Map<String, Object>> matches = CaregiverMatches.find(connection, requestId, caregiverId);
                if (matches.isEmpty()) {
                    connection.rollback();
                    response.sendRedirect(request.getContextPath() + "/owner/matches?requestId=" + requestId + "&unavailable=1");
                    return;
                }
                Map<String, Object> match = matches.get(0);
                int bookingId;
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO Bookings (caregiver_id, request_id, status, total_price) VALUES (?, ?, 'pending', ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    insert.setInt(1, caregiverId);
                    insert.setInt(2, requestId);
                    insert.setBigDecimal(3, (java.math.BigDecimal) match.get("total"));
                    insert.executeUpdate();
                    try (ResultSet keys = insert.getGeneratedKeys()) {
                        keys.next();
                        bookingId = keys.getInt(1);
                    }
                }
                @SuppressWarnings("unchecked")
                List<String> slots = (List<String>) match.get("slots");
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO Visits (visit_date, visit_time, booking_id) VALUES (?, ?, ?)")) {
                    for (String slot : slots) {
                        insert.setString(1, slot.substring(0, 10));
                        insert.setString(2, slot.substring(11, 16));
                        insert.setInt(3, bookingId);
                        insert.addBatch();
                    }
                    insert.executeBatch();
                }
                try (PreparedStatement update = connection.prepareStatement(
                        "UPDATE CareRequests SET status = 'held' WHERE request_id = ?")) {
                    update.setInt(1, requestId);
                    update.executeUpdate();
                }
                connection.commit();
                response.sendRedirect(request.getContextPath() + "/owner/bookings?created=1");
            } catch (SQLException error) {
                connection.rollback();
                throw error;
            }
        } catch (SQLException error) {
            throw new ServletException("Could not save the booking.", error);
        }
    }
}
