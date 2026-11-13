SET FOREIGN_KEY_CHECKS = 0;

ALTER TABLE Caregivers
    ADD COLUMN service_area VARCHAR(200),
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'pending';

ALTER TABLE Bookings
    MODIFY booking_id INT NOT NULL AUTO_INCREMENT,
    ADD UNIQUE (request_id),
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'pending',
    ADD COLUMN total_price DECIMAL(10,2) NOT NULL DEFAULT 0;

ALTER TABLE Visits
    MODIFY visit_id INT NOT NULL AUTO_INCREMENT,
    ADD COLUMN visit_time VARCHAR(10);

SET FOREIGN_KEY_CHECKS = 1;

UPDATE Bookings SET status = 'accepted';

UPDATE Bookings b, CareRequests r, CaregiverServices s
SET b.total_price = s.price * (SELECT COUNT(*) FROM Visits v WHERE v.booking_id = b.booking_id)
WHERE b.request_id = r.request_id AND s.caregiver_id = b.caregiver_id
AND s.service_id = r.service_id;
