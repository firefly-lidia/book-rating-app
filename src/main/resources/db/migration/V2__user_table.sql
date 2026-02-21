CREATE SEQUENCE users_seq START 1 INCREMENT 1;

CREATE TABLE users (
    id           BIGINT PRIMARY KEY DEFAULT nextval('users_seq'),
    email        TEXT NOT NULL,
    password     TEXT NOT NULL,
    role         TEXT NOT NULL,
    name         TEXT NOT NULL,
    surname      TEXT NOT NULL,
    nickname     TEXT NOT NULL,
    age          SMALLINT NOT NULL CHECK (age BETWEEN 0 AND 123),
    rec_version  INTEGER       NOT NULL,
    created_by   VARCHAR(255)  NOT NULL,
    created_ts   TIMESTAMPTZ   NOT NULL,
    modified_by  VARCHAR(255),
    modified_ts  TIMESTAMPTZ,

    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_nickname UNIQUE (nickname)
);