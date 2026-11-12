package pawmatch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CaregiverMatches {
    public static List<Map<String, Object>> find(Connection connection, int requestId, int caregiverId)
            throws SQLException {
        List<String[]> visits = new ArrayList<>();
        try (PreparedStatement query = connection.prepareStatement(
                "SELECT visit_date, visit_time FROM RequestVisits WHERE request_id = ? "
                + "ORDER BY visit_date, (visit_time IS NULL OR visit_time = ''), visit_time, request_visit_id")) {
            query.setInt(1, requestId);
            try (ResultSet rows = query.executeQuery()) {
                while (rows.next()) {
                    String date = rows.getString("visit_date");
                    String time = rows.getString("visit_time");
                    try {
                        LocalDate.parse(date);
                        if (time != null && !time.isBlank()) {
                            time = LocalTime.parse(time).format(DateTimeFormatter.ofPattern("HH:mm"));
                        }
                    } catch (DateTimeParseException | NullPointerException error) {
                        return new ArrayList<>();
                    }
                    visits.add(new String[] {date, time});
                }
            }
        }
        List<Map<String, Object>> matches = new ArrayList<>();
        if (visits.isEmpty()) return matches;
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        try (PreparedStatement query = connection.prepareStatement(
                "SELECT c.caregiver_id, c.name, c.service_area, s.price "
                + "FROM Caregivers c, CaregiverServices s, CaregiverAnimalTypes t, CareRequests r, Pets p "
                + "WHERE r.request_id = ? AND r.pet_id = p.pet_id AND p.active = 1 AND r.status = 'open' "
                + "AND s.caregiver_id = c.caregiver_id AND s.service_id = r.service_id "
                + "AND t.caregiver_id = c.caregiver_id AND LOWER(t.animal_type) = LOWER(p.species) "
                + "AND LOWER(TRIM(c.service_area)) = LOWER(TRIM(r.location)) AND c.status = 'active' "
                + "AND s.price >= 0 AND (? = 0 OR c.caregiver_id = ?) ORDER BY s.price, c.name")) {
            query.setInt(1, requestId);
            query.setInt(2, caregiverId);
            query.setInt(3, caregiverId);
            try (ResultSet rows = query.executeQuery()) {
                while (rows.next()) {
                    List<String> slots = new ArrayList<>();
                    Set<String> used = new HashSet<>();
                    for (String[] visit : visits) {
                        try (PreparedStatement available = connection.prepareStatement(
                                "SELECT a.start_time FROM Availability a "
                                + "WHERE a.caregiver_id = ? AND SUBSTRING(a.start_time, 1, 10) = ? "
                                + "AND (? IS NULL OR ? = '' OR SUBSTRING(a.start_time, 12, 5) = ?) "
                                + "AND a.start_time > ? AND NOT EXISTS ("
                                + "SELECT 1 FROM Bookings b, Visits v WHERE b.booking_id = v.booking_id "
                                + "AND b.caregiver_id = a.caregiver_id AND b.status IN ('pending', 'accepted', 'active') "
                                + "AND v.visit_date = SUBSTRING(a.start_time, 1, 10) "
                                + "AND (v.visit_time IS NULL OR SUBSTRING(v.visit_time, 1, 5) = SUBSTRING(a.start_time, 12, 5))) "
                                + "ORDER BY a.start_time")) {
                            available.setInt(1, rows.getInt("caregiver_id"));
                            available.setString(2, visit[0]);
                            for (int i = 3; i <= 5; i++) available.setString(i, visit[1]);
                            available.setString(6, now);
                            try (ResultSet times = available.executeQuery()) {
                                while (times.next()) {
                                    String start = times.getString("start_time");
                                    if (used.add(start)) {
                                        slots.add(start);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (slots.size() == visits.size()) {
                        java.util.Collections.sort(slots);
                        Map<String, Object> match = new LinkedHashMap<>();
                        match.put("id", rows.getInt("caregiver_id"));
                        match.put("name", rows.getString("name"));
                        match.put("area", rows.getString("service_area"));
                        match.put("price", rows.getBigDecimal("price"));
                        match.put("total", rows.getBigDecimal("price").multiply(java.math.BigDecimal.valueOf(slots.size())));
                        match.put("slots", slots);
                        matches.add(match);
                    }
                }
            }
        }
        return matches;
    }
}
