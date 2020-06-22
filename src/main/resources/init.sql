DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO clinic;
GRANT ALL ON SCHEMA public TO public;

CREATE TABLE doctor
(
    id                  SERIAL PRIMARY KEY,
    version             INTEGER               DEFAULT 0,
    name                VARCHAR(100) NOT NULL
    );
