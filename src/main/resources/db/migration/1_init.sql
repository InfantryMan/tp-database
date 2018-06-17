CREATE EXTENSION IF NOT EXISTS citext;

CREATE TABLE IF NOT EXISTS users (
  nickname CITEXT UNIQUE NOT NULL PRIMARY KEY,
  email CITEXT UNIQUE NOT NULL,
  fullname VARCHAR(128) NOT NULL,
  about TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS forum (
  posts BIGINT NOT NULL DEFAULT 0,
  slug CITEXT UNIQUE NOT NULL PRIMARY KEY,
  threads INTEGER NOT NULL DEFAULT 0,
  title VARCHAR(128) NOT NULL,
  admin CITEXT NOT NULL,
  FOREIGN KEY (admin) REFERENCES users(nickname)
);

CREATE TABLE IF NOT EXISTS threads (
  author CITEXT NOT NULL,
  created TIMESTAMP NOT NULL DEFAULT current_timestamp,
  forum CITEXT NOT NULL,
  id BIGSERIAL UNIQUE NOT NULL PRIMARY KEY,
  message TEXT NOT NULL,
  slug CITEXT UNIQUE,
  title VARCHAR(128) NOT NULL,
  votes INTEGER NOT NULL DEFAULT 0,
  FOREIGN KEY (author) REFERENCES users(nickname),
  FOREIGN KEY (forum) REFERENCES forum(slug)
);

CREATE TABLE IF NOT EXISTS posts (
  author CITEXT NOT NULL,
  created TIMESTAMP NOT NULL DEFAULT current_timestamp,
  forum CITEXT NOT NULL,
  id BIGSERIAL UNIQUE NOT NULL PRIMARY KEY,
  isEdited BOOLEAN NOT NULL DEFAULT FALSE,
  message TEXT NOT NULL,
  parent BIGINT NOT NULL DEFAULT  0,
  thread BIGSERIAL NOT NULL,
  post_path INTEGER[],
  FOREIGN KEY (author) REFERENCES users(nickname),
  FOREIGN KEY (forum) REFERENCES forum(slug),
  FOREIGN KEY (thread) REFERENCES threads(id)
);

CREATE TABLE IF NOT EXISTS users_forum (
  author CITEXT NOT NULL,
  forum CITEXT NOT NULL
);

CREATE OR REPLACE FUNCTION insert_users_forum(insert_forum CITEXT, insert_author CITEXT) RETURNS void AS '
DECLARE
  have INT;
BEGIN
  SELECT COUNT(author) INTO have FROM users_forum
  WHERE lower(forum) = lower(insert_forum) AND lower(author) = lower(insert_author) LIMIT 1;
  IF have = 0 THEN
    INSERT INTO users_forum(forum, author) VALUES(insert_forum, insert_author);
  END IF;
END;
' LANGUAGE plpgsql;