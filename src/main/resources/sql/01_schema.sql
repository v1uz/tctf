CREATE TABLE IF NOT EXISTS accounts (
    id BIGSERIAL NOT NULL PRIMARY KEY,
    login VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(1024) NOT NULL,
    tariff DECIMAL(6,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS measurements (
    id BIGSERIAL NOT NULL,
    account_id BIGINT NOT NULL REFERENCES accounts (id),
    measurement VARCHAR(127),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS invoices (
    id BIGSERIAL NOT NULL,
    account_id BIGINT NOT NULL REFERENCES accounts (id),
    total DECIMAL(20,2) NOT NULL,
    paid BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);