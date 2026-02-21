CREATE SEQUENCE rating_seq START 1 INCREMENT 1;

CREATE TABLE rating (
    id           BIGINT PRIMARY KEY DEFAULT nextval('rating_seq'),
    score        SMALLINT  NOT NULL CHECK (score BETWEEN 0 AND 10),
    description  TEXT,
    archived     BOOLEAN   NOT NULL,
    user_id      BIGINT    NOT NULL,
    book_id     BIGINT    NOT NULL,
    rec_version   INTEGER       NOT NULL,
    created_by    VARCHAR(255)  NOT NULL,
    created_ts    TIMESTAMPTZ   NOT NULL,
    modified_by   VARCHAR(255),
    modified_ts   TIMESTAMPTZ,

    CONSTRAINT uk_rating_user_book UNIQUE (user_id, book_id),

    CONSTRAINT fk_rating_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_rating_book FOREIGN KEY (book_id) REFERENCES book(id) ON DELETE RESTRICT
);