SET search_path = book_rating, public;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- TRUNCATE TABLE rating, book, users RESTART IDENTITY CASCADE;

INSERT INTO users (id, email, password, role, name, surname, nickname, age, rec_version, created_by, created_ts) VALUES
                                                                                                                     (1 , 'admin1@example.com', crypt('admin1pass1', gen_salt('bf', 11)), 'ADMIN', 'Alice', 'Admin', 'admin1', 30, 0, 'seed', now()),
                                                                                                                     (2 , 'admin2@example.com', crypt('admin2pass1', gen_salt('bf', 11)), 'ADMIN', 'Bob',   'Admin', 'admin2', 31, 0, 'seed', now()),
                                                                                                                     (3 , 'user1@example.com',  crypt('user1pass1', gen_salt('bf', 11)),  'USER',  'User1', 'Demo',  'user1',  21, 0, 'seed', now()),
                                                                                                                     (4 , 'user2@example.com',  crypt('user2pass1', gen_salt('bf', 11)),  'USER',  'User2', 'Demo',  'user2',  22, 0, 'seed', now()),
                                                                                                                     (5 , 'user3@example.com',  crypt('user3pass1', gen_salt('bf', 11)),  'USER',  'User3', 'Demo',  'user3',  23, 0, 'seed', now()),
                                                                                                                     (6 , 'user4@example.com',  crypt('user4pass1', gen_salt('bf', 11)),  'USER',  'User4', 'Demo',  'user4',  24, 0, 'seed', now()),
                                                                                                                     (7 , 'user5@example.com',  crypt('user5pass1', gen_salt('bf', 11)),  'USER',  'User5', 'Demo',  'user5',  25, 0, 'seed', now()),
                                                                                                                     (8 , 'user6@example.com',  crypt('user6pass1', gen_salt('bf', 11)),  'USER',  'User6', 'Demo',  'user6',  26, 0, 'seed', now()),
                                                                                                                     (9 , 'user7@example.com',  crypt('user7pass1', gen_salt('bf', 11)),  'USER',  'User7', 'Demo',  'user7',  27, 0, 'seed', now()),
                                                                                                                     (10, 'user8@example.com',  crypt('user8pass1', gen_salt('bf', 11)),  'USER',  'User8', 'Demo',  'user8',  28, 0, 'seed', now());

SELECT setval('users_seq', (SELECT max(id) FROM users));
