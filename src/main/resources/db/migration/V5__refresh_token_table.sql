CREATE SEQUENCE refresh_token_seq START 1 INCREMENT 1;

CREATE TABLE refresh_token (
    id           BIGINT PRIMARY KEY DEFAULT nextval('refresh_token_seq'),
    user_id      BIGINT NOT NULL,
    jti          UUID NOT NULL,
    hashedToken  TEXT NOT NULL,
    revoked      BOOLEAN NOT NULL,
    issued_at    TIMESTAMPTZ   NOT NULL,
    expires_at   TIMESTAMPTZ   NOT NULL,
    rec_version  INTEGER       NOT NULL,
    created_by   VARCHAR(255)  NOT NULL,
    created_ts   TIMESTAMPTZ   NOT NULL,
    modified_by  VARCHAR(255),
    modified_ts  TIMESTAMPTZ,

    CONSTRAINT uk_refresh_token_hashed_token UNIQUE (hashedToken),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
);