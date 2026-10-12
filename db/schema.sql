CREATE DATABASE IF NOT EXISTS pawmatch CHARACTER SET utf8mb4;

USE pawmatch;

CREATE TABLE Owners (
    owner_id     INT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    email        VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(20)
);

CREATE TABLE Services (
    service_id INT AUTO_INCREMENT PRIMARY KEY,
    category   VARCHAR(50)
);

CREATE TABLE Caregivers (
    caregiver_id INT AUTO_INCREMENT PRIMARY KEY,
    hourly_rate  DECIMAL(6,2),
    name         VARCHAR(100) NOT NULL,
    email        VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(20)
);

CREATE TABLE CaregiverAnimalTypes (
    caregiver_id INT NOT NULL,
    animal_type  VARCHAR(30) NOT NULL,
    PRIMARY KEY (caregiver_id, animal_type),
    FOREIGN KEY (caregiver_id) REFERENCES Caregivers(caregiver_id)
);

CREATE TABLE Pets (
    pet_id       INT AUTO_INCREMENT PRIMARY KEY,
    owner_id     INT NOT NULL,
    contact_name VARCHAR(100),
    conditions   VARCHAR(200),
    species      VARCHAR(30),
    name         VARCHAR(50) NOT NULL,
    FOREIGN KEY (owner_id) REFERENCES Owners(owner_id)
);

CREATE TABLE CareRequests (
    request_id INT AUTO_INCREMENT PRIMARY KEY,
    service_id INT NOT NULL,
    pet_id     INT NOT NULL,
    FOREIGN KEY (service_id) REFERENCES Services(service_id),
    FOREIGN KEY (pet_id) REFERENCES Pets(pet_id)
);

CREATE TABLE Bookings (
    booking_id   INT AUTO_INCREMENT PRIMARY KEY,
    caregiver_id INT NOT NULL,
    request_id   INT NOT NULL UNIQUE,
    FOREIGN KEY (caregiver_id) REFERENCES Caregivers(caregiver_id),
    FOREIGN KEY (request_id) REFERENCES CareRequests(request_id)
);

CREATE TABLE Visits (
    visit_id   INT AUTO_INCREMENT PRIMARY KEY,
    visit_date DATE,
    booking_id INT NOT NULL,
    FOREIGN KEY (booking_id) REFERENCES Bookings(booking_id)
);

CREATE TABLE CareTasks (
    task_id   INT AUTO_INCREMENT PRIMARY KEY,
    completed BOOLEAN DEFAULT FALSE,
    visit_id  INT NOT NULL,
    FOREIGN KEY (visit_id) REFERENCES Visits(visit_id)
);

CREATE TABLE Availability (
    availability_id INT AUTO_INCREMENT PRIMARY KEY,
    start_time      DATETIME,
    caregiver_id    INT NOT NULL,
    FOREIGN KEY (caregiver_id) REFERENCES Caregivers(caregiver_id)
);

CREATE TABLE CaregiverServices (
    caregiver_id INT NOT NULL,
    service_id   INT NOT NULL,
    price        DECIMAL(6,2),
    PRIMARY KEY (caregiver_id, service_id),
    FOREIGN KEY (caregiver_id) REFERENCES Caregivers(caregiver_id),
    FOREIGN KEY (service_id) REFERENCES Services(service_id)
);

CREATE TABLE Reviews (
    review_id  INT AUTO_INCREMENT PRIMARY KEY,
    rating     INT CHECK (rating BETWEEN 1 AND 5),
    booking_id INT NOT NULL UNIQUE,
    FOREIGN KEY (booking_id) REFERENCES Bookings(booking_id)
);

CREATE TABLE Payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    amount     DECIMAL(8,2),
    booking_id INT NOT NULL,
    FOREIGN KEY (booking_id) REFERENCES Bookings(booking_id)
);

CREATE TABLE Messages (
    message_id INT AUTO_INCREMENT PRIMARY KEY,
    sent_at    DATETIME,
    booking_id INT NOT NULL,
    FOREIGN KEY (booking_id) REFERENCES Bookings(booking_id)
);
