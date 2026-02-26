CREATE SEQUENCE book_seq START 1 INCREMENT 1;

CREATE TABLE book (
    id           BIGINT PRIMARY KEY DEFAULT nextval('book_seq'),
    title        TEXT NOT NULL,
    author       TEXT NOT NULL,
    genre        TEXT NOT NULL,
    publication_year INTEGER NOT NULL,
    description   TEXT NOT NULL ,
    isbn         VARCHAR(20) NOT NULL,
    publisher     TEXT NOT NULL,
    language      VARCHAR(2) NOT NULL,
    number_of_pages INTEGER NOT NULL,
    price         NUMERIC(10,2) NOT NULL,
    currency      VARCHAR(3)    NOT NULL,
    rec_version  INTEGER       NOT NULL,
    created_by   VARCHAR(255)  NOT NULL,
    created_ts   TIMESTAMPTZ   NOT NULL,
    modified_by  VARCHAR(255),
    modified_ts  TIMESTAMPTZ,

    CONSTRAINT uk_book_isbn UNIQUE (isbn)
);

CREATE INDEX idx_book_title  ON book(title);
CREATE INDEX idx_book_author ON book(genre );