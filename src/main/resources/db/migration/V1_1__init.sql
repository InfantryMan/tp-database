CREATE EXTENSION IF NOT EXISTS citext;

CREATE TABLE IF NOT EXISTS public.users (
  nickname CITEXT PRIMARY KEY,
  email CITEXT UNIQUE NOT NULL,
  fullname VARCHAR(128) NOT NULL,
  about TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS public.forum (
  posts BIGINT NOT NULL DEFAULT 0,
  slug CITEXT PRIMARY KEY,
  threads INTEGER NOT NULL DEFAULT 0,
  title VARCHAR(128) NOT NULL,
  admin CITEXT NOT NULL,
  FOREIGN KEY (admin) REFERENCES users(nickname)
);

CREATE TABLE IF NOT EXISTS public.threads (
  author CITEXT NOT NULL,
  created TIMESTAMP NOT NULL DEFAULT current_timestamp,
  forum CITEXT NOT NULL,
  id BIGSERIAL PRIMARY KEY,
  message TEXT NOT NULL,
  slug CITEXT UNIQUE,
  title VARCHAR(128) NOT NULL,
  votes INTEGER NOT NULL DEFAULT 0,
  FOREIGN KEY (author) REFERENCES users(nickname),
  FOREIGN KEY (forum) REFERENCES forum(slug)
);

CREATE TABLE IF NOT EXISTS public.posts (
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

CREATE TABLE IF NOT EXISTS public.users_forum (
  author CITEXT,
  forum CITEXT,
  UNIQUE(author, forum)
);

CREATE TABLE IF NOT EXISTS votes (
  nickname CITEXT NOT NULL,
  voice BIGINT NOT NULL,
  thread BIGINT NOT NULL,
  FOREIGN KEY (nickname) REFERENCES  users(nickname),
  FOREIGN KEY (thread) REFERENCES threads(id),
  PRIMARY KEY (nickname, voice, thread)
);

