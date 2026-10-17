CREATE TABLE Accounts (
    account_id    INT PRIMARY KEY,
    email         VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20) NOT NULL,
    owner_id      INT,
    caregiver_id  INT,
    FOREIGN KEY (owner_id) REFERENCES Owners(owner_id),
    FOREIGN KEY (caregiver_id) REFERENCES Caregivers(caregiver_id)
);
