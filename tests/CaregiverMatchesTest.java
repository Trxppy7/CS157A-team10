package pawmatch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

public class CaregiverMatchesTest {
    private static void sql(Connection connection, String sql) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    private static void check(Connection connection, int expected, String message) throws Exception {
        int actual = CaregiverMatches.find(connection, 900001, 900001).size();
        if (actual != expected) throw new AssertionError(message + ": expected " + expected + ", got " + actual);
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Pass the JDBC URL of a test database with the schema installed.");
        try (Connection connection = DriverManager.getConnection(args[0],
                System.getenv().getOrDefault("DB_USER", "root"), System.getenv().getOrDefault("DB_PASSWORD", ""))) {
            connection.setAutoCommit(false);
            try {
                sql(connection, "INSERT INTO Owners VALUES (900001, 'Test owner', 'owner@example.test', '4085550111')");
                sql(connection, "INSERT INTO Services VALUES (900001, 'Walking')");
                sql(connection, "INSERT INTO Caregivers (caregiver_id, name, email, service_area, status) VALUES (900001, 'Test caregiver', 'caregiver@example.test', 'San Jose, CA', 'active')");
                sql(connection, "INSERT INTO Pets (pet_id, owner_id, species, name) VALUES (900001, 900001, 'Dog', 'Test pet')");
                sql(connection, "INSERT INTO CaregiverServices VALUES (900001, 900001, 24)");
                sql(connection, "INSERT INTO CaregiverAnimalTypes VALUES (900001, 'Dog')");
                sql(connection, "INSERT INTO CareRequests (request_id, service_id, pet_id, location) VALUES (900001, 900001, 900001, ' san jose, ca ')");
                sql(connection, "INSERT INTO RequestVisits VALUES (900001, 900001, '2099-01-01', NULL), (900002, 900001, '2099-01-01', '09:00')");
                sql(connection, "INSERT INTO Availability VALUES (900001, '2099-01-01 09:00:00', 900001), (900002, '2099-01-01 10:00:00', 900001)");
                check(connection, 1, "Exact times must be allocated before flexible visits");
                List<Map<String, Object>> matches = CaregiverMatches.find(connection, 900001, 0);
                if (matches.size() != 1 || !matches.get(0).get("total").toString().startsWith("48")) {
                    throw new AssertionError("Price must include both visits");
                }
                sql(connection, "UPDATE Caregivers SET service_area = 'Oakland, CA' WHERE caregiver_id = 900001");
                check(connection, 0, "Wrong city");
                sql(connection, "UPDATE Caregivers SET service_area = 'San Jose, CA', status = 'pending' WHERE caregiver_id = 900001");
                check(connection, 0, "Unapproved caregiver");
                sql(connection, "UPDATE Caregivers SET status = 'active' WHERE caregiver_id = 900001");
                sql(connection, "UPDATE CaregiverAnimalTypes SET animal_type = 'Cat' WHERE caregiver_id = 900001");
                check(connection, 0, "Wrong animal experience");
                sql(connection, "UPDATE CaregiverAnimalTypes SET animal_type = 'Dog' WHERE caregiver_id = 900001");
                sql(connection, "UPDATE CaregiverServices SET price = -1 WHERE caregiver_id = 900001");
                check(connection, 0, "Invalid price");
                sql(connection, "UPDATE CaregiverServices SET price = 24 WHERE caregiver_id = 900001");
                sql(connection, "DELETE FROM CaregiverServices WHERE caregiver_id = 900001");
                check(connection, 0, "Missing service");
                sql(connection, "INSERT INTO CaregiverServices VALUES (900001, 900001, 24)");
                sql(connection, "DELETE FROM Availability WHERE availability_id = 900002");
                check(connection, 0, "Not enough separate slots");
                sql(connection, "INSERT INTO Availability VALUES (900002, '2099-01-01 10:00:00', 900001)");
                sql(connection, "UPDATE Pets SET active = 0 WHERE pet_id = 900001");
                check(connection, 0, "Inactive pet");
                sql(connection, "UPDATE Pets SET active = 1 WHERE pet_id = 900001");
                sql(connection, "UPDATE CareRequests SET status = 'held' WHERE request_id = 900001");
                check(connection, 0, "Already held request");
                sql(connection, "UPDATE CareRequests SET status = 'open' WHERE request_id = 900001");
                sql(connection, "INSERT INTO Bookings (booking_id, caregiver_id, request_id) VALUES (900001, 900001, 900001)");
                sql(connection, "INSERT INTO Visits (visit_date, visit_time, booking_id) VALUES ('2099-01-01', '09:00', 900001)");
                check(connection, 0, "Pending bookings reserve their slots");
                sql(connection, "UPDATE Bookings SET status = 'canceled' WHERE booking_id = 900001");
                check(connection, 1, "Canceled bookings release slots");
                sql(connection, "UPDATE RequestVisits SET visit_date = '2000-01-01' WHERE request_id = 900001");
                check(connection, 0, "Past visits");
                sql(connection, "UPDATE RequestVisits SET visit_date = '2099-02-30' WHERE request_id = 900001");
                check(connection, 0, "Invalid calendar date");
                System.out.println("Caregiver matching checks passed.");
            } finally {
                connection.rollback();
            }
        }
    }
}
