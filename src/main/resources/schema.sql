CREATE TABLE standort (
    standort_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    adresse VARCHAR(255) NOT NULL,
    telefon VARCHAR(50),
    email VARCHAR(100),
    status VARCHAR(50)
);
