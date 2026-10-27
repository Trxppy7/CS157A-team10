CREATE TABLE IF NOT EXISTS PasswordResetTokens (
    reset_id INT AUTO_INCREMENT PRIMARY KEY,
    account_id INT NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at DATETIME NOT NULL,
    used_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (account_id)
        REFERENCES Accounts(account_id)
        ON DELETE CASCADE
);
