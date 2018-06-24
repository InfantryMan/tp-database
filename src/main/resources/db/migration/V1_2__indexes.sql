create index idx_users_forum_author_forum ON users_forum(lower(author), lower(forum));
create index idx_users_nickname ON users USING hash(nickname);

create index idx_threads_slug ON threads(lower(slug));
create index idx_votes_thread_nickname_voice ON votes(thread, nickname, voice);

create index idx_posts_id_post_path ON posts(id,post_path);
create index idx_posts_thread_post_path ON posts(thread, post_path);
create index idx_posts_thread_parent ON posts(thread, parent);

create index idx_posts_post_path1 ON posts ((post_path[1]));
create index idx_posts_post_path1_post_path ON posts ((post_path[1]), post_path);
create index idx_posts_id__desc ON posts (id DESC);
create index idx_users_nickname_full ON users (lower((nickname)::text), email, fullname, about);
create index idx_posts_id_post_path1 ON posts (id, (post_path[1]));
create index idx_users_forum_forum ON users_forum (lower((forum)::text));
create index idx_threads_forum ON threads (lower((forum)::text))
