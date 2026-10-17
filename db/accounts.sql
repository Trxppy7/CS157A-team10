CREATE TABLE IF NOT EXISTS Accounts (
    account_id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    owner_id INT UNIQUE,
    caregiver_id INT UNIQUE,
    FOREIGN KEY (owner_id) REFERENCES Owners(owner_id),
    FOREIGN KEY (caregiver_id) REFERENCES Caregivers(caregiver_id),
    CHECK ((role = 'owner' AND owner_id IS NOT NULL AND caregiver_id IS NULL)
        OR (role = 'caregiver' AND caregiver_id IS NOT NULL AND owner_id IS NULL)
        OR (role = 'admin' AND owner_id IS NULL AND caregiver_id IS NULL))
);
