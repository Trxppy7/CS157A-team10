-- Local development only. This deletes all PawMatch data, including new accounts.
-- Run with schema.sql and seed.sql as shown in README.md.
DROP TABLE IF EXISTS PasswordResetTokens;
DROP TABLE IF EXISTS OwnerDetails;
DROP TABLE Accounts;
DROP TABLE Messages;
DROP TABLE Payments;
DROP TABLE Reviews;
DROP TABLE CaregiverServices;
DROP TABLE Availability;
DROP TABLE CareTasks;
DROP TABLE Visits;
DROP TABLE Bookings;
DROP TABLE CareRequests;
DROP TABLE Pets;
DROP TABLE CaregiverAnimalTypes;
DROP TABLE Caregivers;
DROP TABLE Services;
DROP TABLE Owners;
