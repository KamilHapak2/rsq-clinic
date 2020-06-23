DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO clinic;
GRANT ALL ON SCHEMA public TO public;

CREATE TABLE doctor
(
    id                  SERIAL PRIMARY KEY,
    version             INTEGER DEFAULT 0,
    name                VARCHAR(128) NOT NULL,
    surname             VARCHAR(128) NOT NULL,
    spec                VARCHAR(128) NOT NULL
);

CREATE TABLE patient
(
    id                  SERIAL PRIMARY KEY,
    version             INTEGER DEFAULT 0,
    name                VARCHAR(128) NOT NULL,
    surname             VARCHAR(128) NOT NULL,
    address             VARCHAR(128) NOT NULL
);

CREATE TABLE visit
(
    id                  SERIAL PRIMARY KEY,
    version             INTEGER DEFAULT 0,
    date_time           TIMESTAMP,
    location            VARCHAR(128) NOT NULL,
    doctor_id           BIGINT NOT NULL,
    patient_id          BIGINT NOT NULL
);

