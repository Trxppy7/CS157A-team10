INSERT INTO Owners (owner_id, name, email, phone_number) VALUES
(1, 'Maya Patel', 'maya.patel@example.com', '408-555-0101'),
(2, 'Daniel Kim', 'daniel.kim@example.com', '408-555-0102'),
(3, 'Sofia Ramirez', 'sofia.ramirez@example.com', '669-555-0103'),
(4, 'Ethan Williams', 'ethan.williams@example.com', '650-555-0104'),
(5, 'Olivia Chen', 'olivia.chen@example.com', '415-555-0105'),
(6, 'Noah Thompson', 'noah.thompson@example.com', '510-555-0106'),
(7, 'Ava Martinez', 'ava.martinez@example.com', '925-555-0107'),
(8, 'Liam Johnson', 'liam.johnson@example.com', '707-555-0108'),
(9, 'Emma Davis', 'emma.davis@example.com', '831-555-0109'),
(10, 'Lucas Brown', 'lucas.brown@example.com', '209-555-0110');

INSERT INTO Services (service_id, category) VALUES
(1, 'Dog Walking'),
(2, 'Drop-In Visit'),
(3, 'Overnight Pet Sitting'),
(4, 'Dog Day Care'),
(5, 'Cat Sitting'),
(6, 'Puppy Care'),
(7, 'Senior Pet Care'),
(8, 'Medication Administration'),
(9, 'Pet Transportation'),
(10, 'Grooming Visit');

INSERT INTO Caregivers (caregiver_id, hourly_rate, name, email, phone_number) VALUES
(1, 24, 'Grace Lee', 'grace.lee@example.com', '408-555-0201'),
(2, 23, 'Marcus Reed', 'marcus.reed@example.com', '408-555-0202'),
(3, 28, 'Isabella Nguyen', 'isabella.nguyen@example.com', '669-555-0203'),
(4, 21, 'Henry Wilson', 'henry.wilson@example.com', '650-555-0204'),
(5, 24, 'Natalie Garcia', 'natalie.garcia@example.com', '415-555-0205'),
(6, 30, 'Caleb Anderson', 'caleb.anderson@example.com', '510-555-0206'),
(7, 28, 'Zoe Hernandez', 'zoe.hernandez@example.com', '925-555-0207'),
(8, 26, 'Owen Clark', 'owen.clark@example.com', '707-555-0208'),
(9, 25, 'Layla Lewis', 'layla.lewis@example.com', '831-555-0209'),
(10, 25, 'Julian Walker', 'julian.walker@example.com', '209-555-0210');

INSERT INTO CaregiverAnimalTypes (caregiver_id, animal_type) VALUES
(1, 'Dog'),
(2, 'Cat'),
(3, 'Dog'),
(4, 'Rabbit'),
(5, 'Bird'),
(6, 'Dog'),
(7, 'Cat'),
(8, 'Dog'),
(9, 'Dog'),
(10, 'Cat');

INSERT INTO Pets (pet_id, owner_id, contact_name, conditions, species, name) VALUES
(1, 1, 'Ravi Patel', 'No known medical conditions', 'Dog', 'Buddy'),
(2, 2, 'Hannah Kim', 'Mild food allergy to chicken', 'Cat', 'Luna'),
(3, 3, 'Mateo Ramirez', 'Puppy; requires frequent bathroom breaks', 'Dog', 'Cooper'),
(4, 4, 'Chloe Williams', 'Sensitive stomach', 'Rabbit', 'Daisy'),
(5, 5, 'Eric Chen', 'Wing injury fully healed; avoid rough handling', 'Bird', 'Kiwi'),
(6, 6, 'Amelia Thompson', 'Arthritis in rear legs', 'Dog', 'Max'),
(7, 7, 'Diego Martinez', 'Daily thyroid medication at 6 PM', 'Cat', 'Bella'),
(8, 8, 'Mia Johnson', 'Anxious during car rides', 'Dog', 'Rocky'),
(9, 9, 'James Davis', 'High energy; needs supervised group play', 'Dog', 'Bailey'),
(10, 10, 'Ella Brown', 'No known medical conditions', 'Cat', 'Nala');

INSERT INTO CareRequests (request_id, service_id, pet_id) VALUES
(1, 1, 1),
(2, 5, 2),
(3, 6, 3),
(4, 2, 4),
(5, 2, 5),
(6, 7, 6),
(7, 8, 7),
(8, 9, 8),
(9, 4, 9),
(10, 5, 10);

INSERT INTO Bookings (booking_id, caregiver_id, request_id) VALUES
(1, 1, 1),
(2, 2, 2),
(3, 3, 3),
(4, 4, 4),
(5, 5, 5),
(6, 6, 6),
(7, 7, 7),
(8, 8, 8),
(9, 9, 9),
(10, 10, 10);

INSERT INTO Visits (visit_id, visit_date, booking_id) VALUES
(1, '2026-10-03', 1),
(2, '2026-10-04', 2),
(3, '2026-10-05', 3),
(4, '2026-10-06', 4),
(5, '2026-10-07', 5),
(6, '2026-10-08', 6),
(7, '2026-10-09', 7),
(8, '2026-10-10', 8),
(9, '2026-10-11', 9),
(10, '2026-10-12', 10);

INSERT INTO CareTasks (task_id, completed, visit_id) VALUES
(1, 1, 1),
(2, 1, 2),
(3, 1, 3),
(4, 1, 4),
(5, 1, 5),
(6, 0, 6),
(7, 0, 7),
(8, 0, 8),
(9, 0, 9),
(10, 0, 10);

INSERT INTO Availability (availability_id, start_time, caregiver_id) VALUES
(1, '2026-10-03 08:00:00', 1),
(2, '2026-10-04 09:30:00', 2),
(3, '2026-10-05 07:00:00', 3),
(4, '2026-10-06 12:00:00', 4),
(5, '2026-10-07 10:00:00', 5),
(6, '2026-10-08 14:00:00', 6),
(7, '2026-10-09 17:30:00', 7),
(8, '2026-10-10 11:00:00', 8),
(9, '2026-10-11 08:30:00', 9),
(10, '2026-10-12 15:00:00', 10);

INSERT INTO CaregiverServices (caregiver_id, service_id, price) VALUES
(1, 1, 24),
(2, 5, 45),
(3, 6, 28),
(4, 2, 21),
(5, 2, 24),
(6, 7, 30),
(7, 8, 28),
(8, 9, 52),
(9, 4, 75),
(10, 5, 49);

INSERT INTO Reviews (review_id, rating, booking_id) VALUES
(1, 5, 1),
(2, 4, 2),
(3, 5, 3),
(4, 4, 4),
(5, 5, 5),
(6, 4, 6),
(7, 5, 7),
(8, 4, 8),
(9, 5, 9),
(10, 5, 10);

INSERT INTO Payments (payment_id, amount, booking_id) VALUES
(1, 24, 1),
(2, 45, 2),
(3, 56, 3),
(4, 21, 4),
(5, 24, 5),
(6, 60, 6),
(7, 28, 7),
(8, 52, 8),
(9, 75, 9),
(10, 49, 10);

INSERT INTO Messages (message_id, sent_at, booking_id) VALUES
(1, '2026-10-01 09:15:00', 1),
(2, '2026-10-01 10:05:00', 2),
(3, '2026-10-01 11:20:00', 3),
(4, '2026-10-02 08:45:00', 4),
(5, '2026-10-02 09:30:00', 5),
(6, '2026-10-02 13:10:00', 6),
(7, '2026-10-03 16:25:00', 7),
(8, '2026-10-04 12:40:00', 8),
(9, '2026-10-05 07:55:00', 9),
(10, '2026-10-06 14:35:00', 10);
