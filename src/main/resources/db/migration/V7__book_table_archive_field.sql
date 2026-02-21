ALTER TABLE book
    ADD COLUMN archived BOOLEAN NOT NULL;

CREATE INDEX idx_book_archived ON book(archived);