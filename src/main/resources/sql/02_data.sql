ALTER SEQUENCE accounts_id_seq RESTART WITH 1000;

INSERT INTO accounts (id, login, password, name, address, tariff) VALUES
    (1, '00000001', '<REDACTED>', 'Капибар Капибарович Константинопольский (МЭР)', '[REDACTED]', 0);

INSERT INTO measurements (account_id, measurement) VALUES
    (1, '001337');

INSERT INTO invoices (account_id, total, paid) VALUES
    (1, 0, true);
