package forumapi.databases.models;

public class Queries {
    // User
    public static final String insertUser = "INSERT INTO users (nickname, email, fullname, about) " +
            "VALUES (?, ?, ?, ?) ON CONFLICT DO NOTHING RETURNING *;";
    public static final String selectDubUsers = "SELECT * FROM users WHERE lower(nickname) = lower(?) OR lower(email) = lower(?);";
    public static final String selectUser = "SELECT * FROM users WHERE lower(nickname) = lower(?);";
    public static final String truncateUsers = "TRUNCATE TABLE users, users_forum CASCADE;";
    public static final String selectUsersCount = "SELECT COUNT(nickname) FROM users ;";


    // Thread
    public static final String selectThreadById = "SELECT * FROM threads WHERE id = ?;";
    public static final String selectThreadBySlug = "SELECT * FROM threads WHERE LOWER(slug) = LOWER(?);";
    public static final String updateThreadVote = "UPDATE threads SET votes = votes + ? WHERE id = ?;";
    public static final String truncateThreads = "TRUNCATE TABLE threads CASCADE;";
    public static final String selectThreadsCount = "SELECT COUNT(id) FROM threads;";
    public static final String selectThreadByForumSlug = "SELECT * FROM threads WHERE LOWER(slug) = LOWER(?);";
    public static final String insertThread = "INSERT INTO threads (author, forum, message, title) VALUES (?, ?, ?, ?) RETURNING id;";
    public static final String updateThreadCreated = "UPDATE threads SET created = ? WHERE id = ? ;";
    public static final String updateThreadSlug = "UPDATE threads SET slug = ? WHERE id = ?";

    public static final String selectThreadByForumSlugAsc = "SELECT * FROM threads WHERE LOWER(forum) = LOWER(?) ORDER BY created ASC LIMIT ?";
    public static final String selectThreadByForumSlugDesc = "SELECT * FROM threads WHERE LOWER(forum) = LOWER(?) ORDER BY created DESC LIMIT ?";
    public static final String selectThreadByForumSlugAscSince = "SELECT * FROM threads WHERE LOWER(forum) = LOWER(?) AND created >= ? ORDER BY created ASC LIMIT ?";
    public static final String selectThreadByForumSlugDescSince = "SELECT * FROM threads WHERE LOWER(forum) = LOWER(?) AND created <= ? ORDER BY created DESC LIMIT ?";

    // Post
    public static final String selectPostById = "SELECT * FROM posts WHERE id = ?;";
    public static final String selectPostSeq = "SELECT nextval('posts_id_seq');";
    public static final String insertPost = "INSERT INTO posts(id, parent, author, message, thread, forum, created, post_path) " +
                                            "VALUES(?,?,?,?,?,?,?, array_append((SELECT post_path FROM posts WHERE id = ?), ?) );";
    public static final String truncatePosts = "TRUNCATE TABLE posts CASCADE; ";
    public static final String selectPostsCount = "SELECT COUNT(id) FROM POSTS;";
    public static final String updatePost = "UPDATE posts SET isEdited = true, message = ? WHERE id = ? RETURNING *;";

    // Forum
    public static final String updateForumPosts = "UPDATE forum SET posts = posts + ? WHERE slug = ?; ";
    public static final String truncateForums = "TRUNCATE TABLE forum CASCADE; ";
    public static final String selectForumsCount = "SELECT COUNT (slug) FROM forum;";
    public static final String selectForum = "SELECT * FROM forum WHERE LOWER(slug) = LOWER(?);";
    public static final String insertForum = "INSERT INTO forum (admin, title, slug) VALUES (?, ?, ?)";
    public static final String updateForumThreads = "UPDATE forum SET threads = threads + 1 WHERE slug = ?;";

    // Vote
    public static final String selectVote = "SELECT voice FROM votes WHERE thread = ? AND lower(nickname) = lower(?); ";
    public static final String insertVote = "INSERT INTO votes VALUES (?, ?, ?);";
    public static final String updateVote = "UPDATE votes SET voice = ? WHERE thread = ? AND nickname = ?;";
    public static final String truncateVotes = "TRUNCATE TABLE votes CASCADE; ";

    // Users_forum
    public static final String insertToUserForum = "INSERT INTO users_forum(forum, author) VALUES (?, ?) ON CONFLICT DO NOTHING;";
    public static final String selectUserForumCount = "SELECT COUNT(*) FROM users_forum WHERE lower(author) = lower(?) AND lower(forum) = lower(?) LIMIT 1;";

    public static final String selectUserForum = "SELECT author FROM users_forum WHERE lower(forum) = lower(?) " +
            "ORDER BY lower(author COLLATE \"C\") ASC LIMIT ?; ";
    public static final String selectUserForumDesc = "SELECT author FROM users_forum WHERE lower(forum) = lower(?) " +
            "ORDER BY lower(author COLLATE \"C\") DESC LIMIT ?; ";
    public static final String selectUserForumSince = "SELECT author FROM users_forum WHERE lower(forum) = lower(?) AND lower(author COLLATE \"C\") > lower(? COLLATE \"C\") " +
            "ORDER BY lower(author COLLATE \"C\") ASC LIMIT ?; ";
    public static final String selectUserForumDescSince = "SELECT author FROM users_forum WHERE lower(forum) = lower(?) AND lower(author COLLATE \"C\") < lower(? COLLATE \"C\") " +
            "ORDER BY lower(author COLLATE \"C\") DESC LIMIT ?; ";


    // Sorts
    public static final String flatSort =   "SELECT * FROM posts WHERE thread = ? " +
                                            "ORDER BY created ASC, id ASC LIMIT ?; ";
    public static final String flatSortDesc =   "SELECT * FROM posts WHERE thread = ? " +
                                                "ORDER BY created DESC, id DESC LIMIT ?; ";
    public static final String flatSortSince =  "SELECT * FROM posts WHERE thread = ? AND id > ? " +
                                                "ORDER BY created ASC, id ASC LIMIT ?; ";
    public static final String flatSortDescSince =  "SELECT * FROM posts WHERE thread = ? AND id < ? " +
                                                    "ORDER BY created DESC, id DESC LIMIT ?; ";

    public static final String treeSort = "SELECT * FROM posts p WHERE thread = ? " +
            "ORDER BY post_path ASC LIMIT ?;  ";
    public static final String treeSortDesc = "SELECT * FROM posts p WHERE thread = ? " +
            "ORDER BY post_path DESC LIMIT ? ; ";
    public static final String treeSortSince = "SELECT * FROM posts p WHERE thread = ? AND post_path > (SELECT post_path FROM posts WHERE id = ?) " +
            "ORDER BY post_path ASC LIMIT ? ; ";
    public static final String treeSortDescSince = "SELECT * FROM posts p WHERE thread = ? AND post_path < (SELECT post_path FROM posts WHERE id = ?) " +
            "ORDER BY post_path DESC LIMIT ? ; ";

    public static final String parentTree = "SELECT * FROM posts p WHERE post_path[1] IN ";
    public static final String parentTreeGetParents = "SELECT id FROM posts WHERE thread = ? AND parent = 0 ";
    public static final String parentTreeGetParentsSince = "SELECT post_path[1] FROM posts WHERE id = ?; ";

    // Other
    public static final String selectCurrentTime = "SELECT current_timestamp;";

}
