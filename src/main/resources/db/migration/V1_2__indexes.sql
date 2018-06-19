create index idx_users_forum_author_forum ON users_forum(lower(author), lower(forum));
create index idx_users_nickname ON users(lower(nickname));
create index idx_threads_slug ON threads(lower(slug));
create index idx_votes_thread_nickname_voice ON votes(thread, nickname, voice);
create index idx_posts_id_post_path ON posts(id,post_path);