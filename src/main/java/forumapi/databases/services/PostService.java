package forumapi.databases.services;

import forumapi.databases.models.Post;
import forumapi.databases.models.PostUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class PostService {
    private final JdbcTemplate jdbc;

    private final String sqlCountPosts = "SELECT COUNT(id) FROM POSTS;";

    private static final  String [] queriesFlatSort = {
            "SELECT * FROM posts WHERE thread = ? " +
            "ORDER BY created ASC, id ASC LIMIT ?; ",

            "SELECT * FROM posts WHERE thread = ? " +
            "ORDER BY created DESC, id DESC LIMIT ?; ",

            "SELECT * FROM posts WHERE thread = ? AND id > ? " +
            "ORDER BY created ASC, id ASC LIMIT ?; ",

            "SELECT * FROM posts WHERE thread = ? AND id < ? " +
            "ORDER BY created DESC, id DESC LIMIT ?; "
    };

    private static final String [] queriesTreeSort = {
            "SELECT * FROM posts p " +
            "WHERE thread = ? ORDER BY post_path ASC LIMIT ?; ",

            "SELECT * FROM posts p " +
            "WHERE thread = ? ORDER BY post_path DESC LIMIT ?; ",

            "SELECT * FROM posts p " +
            "WHERE thread = ? AND post_path > (SELECT post_path FROM posts where id = ?) " +
            "ORDER BY post_path ASC LIMIT ?; ",

            "SELECT * FROM posts p " +
            "WHERE thread = ? AND post_path < (SELECT post_path FROM posts where id = ?) " +
            "ORDER BY post_path DESC LIMIT ?; ",
    };

    private static final String [] queryParentTreeSort = {
            "SELECT * FROM posts p WHERE post_path[1] IN " +
            "(SELECT id FROM posts WHERE thread = ? AND parent = 0 ORDER BY id ASC LIMIT ?) " +
            "ORDER BY post_path[1] ASC, post_path; ",

            "SELECT * FROM posts p WHERE post_path[1] IN " +
            "(SELECT id FROM posts WHERE thread = ? AND parent = 0 ORDER BY id DESC LIMIT ?) " +
            "ORDER BY post_path[1] DESC, post_path; ",

            "SELECT * FROM posts p WHERE post_path[1] IN " +
            "(SELECT id FROM posts WHERE thread = ? AND parent = 0 AND post_path[1] > (SELECT post_path[1] FROM posts WHERE id = ?) ORDER BY id ASC LIMIT ?) " +
            "ORDER BY post_path[1] ASC, post_path; ",

            "SELECT * FROM posts p WHERE post_path[1] IN " +
            "(SELECT id FROM posts WHERE thread = ? AND parent = 0 AND post_path[1] < (SELECT post_path[1] FROM posts WHERE id = ?) ORDER BY id DESC LIMIT ?) " +
            "ORDER BY post_path[1] DESC, post_path; "
    };

    private static Map<String, String[]> queries;

    @Autowired
    public PostService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        queries = new HashMap<>();
        queries.put("flat", queriesFlatSort);
        queries.put("tree", queriesTreeSort);
        queries.put("parent_tree", queryParentTreeSort);
    }

    public Integer getCount() {
        return jdbc.queryForObject(sqlCountPosts, Integer.class);
    }

    public void truncateTable() {
        final String sql = "TRUNCATE TABLE posts CASCADE ;";
        jdbc.execute(sql);
    }

    // Done
    public Post getPostById(Integer id) {
        final String sql = "SELECT * FROM posts WHERE id = ?;";
        Post post = null;
        try {
            post = jdbc.queryForObject(sql, new postMapper(), id);
        } catch (DataAccessException e) {
            return null;
        }
        return post;
    }

    // Done
    public Post updatePost(PostUpdate postUpdate, Integer id) {
        Post oldPost = getPostById(id);

        if (oldPost == null) {
            return null;
        }

        if (postUpdate.getMessage() == null || oldPost.getMessage().equals(postUpdate.getMessage())) {
            return oldPost;
        }

        final String sql = "UPDATE posts SET isEdited = true, message = ? WHERE id = ? RETURNING *;";

        Post post = null;

        try {
            post = jdbc.queryForObject(sql, new postMapper(), postUpdate.getMessage(), id);
        } catch (DataAccessException e) {
            return null;
        }

        return post;
    }

    // Done
    public List<Post> addPostList(List<Post> posts) {
        final String sqlInsertPost = "INSERT INTO posts(id, parent, author, message, thread, forum, created, post_path) " +
                                     "VALUES(?,?,?,?,?,?,?, array_append((SELECT post_path FROM posts WHERE id = ?), ?) );";
        final String sqlUpdateForum = "UPDATE forum SET posts = posts + ? WHERE slug = ?";
        final String sqlSeq =  "SELECT nextval('posts_id_seq');";
        final String sqlTime = "SELECT current_timestamp ;";
        final String sqlInsertUserForum = "SELECT insert_users_forum(?::CITEXT,?::CITEXT)";
        final String sqlInsertPostNoPostPath = "INSERT INTO posts(id, parent, author, message, thread, forum, created, post_path) " +
                "VALUES(?,?,?,?,?,?,?, array_append(?, ?) );";
        final String sqlSelectPostPath = "SELECT post_path FROM posts WHERE id = ?";

        List<Post> newPosts = new ArrayList<>();
        Map<String, Integer> updatedForums = new HashMap<>();

        final Timestamp curr_time = jdbc.queryForObject(sqlTime, Timestamp.class);
        final String time = curr_time.toInstant().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        try {
            jdbc.batchUpdate(sqlInsertPostNoPostPath, new BatchPreparedStatementSetter() {

                @Override
                public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                    Post post = posts.get(i);
                    Integer seq = jdbc.queryForObject(sqlSeq, Integer.class);
                    Array post_path;
                    try {
                        post_path = jdbc.queryForObject(sqlSelectPostPath, Array.class, post.getParent());
                    } catch (DataAccessException e) {
                        post_path = null;
                    }
                    post.setId(seq);
                    post.setCreated(time);
                    preparedStatement.setInt(1, post.getId());
                    preparedStatement.setInt(2, post.getParent());
                    preparedStatement.setString(3, post.getAuthor());
                    preparedStatement.setString(4, post.getMessage());
                    preparedStatement.setInt(5, post.getThread());
                    preparedStatement.setString(6, post.getForum());
                    preparedStatement.setTimestamp(7, curr_time);
                    preparedStatement.setArray(8, post_path);
                    preparedStatement.setInt(9, post.getId());


                    if (!updatedForums.containsKey(post.getForum())) {
                        updatedForums.put(post.getForum(), 1);
                    } else {
                        updatedForums.put(post.getForum(), updatedForums.get(post.getForum()) + 1);
                    }

                    jdbc.queryForObject(sqlInsertUserForum, Object.class, post.getForum(), post.getAuthor());

                    newPosts.add(post);
                }

                @Override
                public int getBatchSize() {
                    return posts.size();
                }
            });
        } catch (DataAccessException e) {
            return null;
        }

//        List<Map.Entry<String, Integer>> updatedForumsList = new ArrayList<>();
//
//        for (Map.Entry<String, Integer> entry: updatedForums.entrySet()) {
//            updatedForumsList.add(entry);
//        }

        List<Map.Entry<String, Integer>> updatedForumsList = new ArrayList<>(updatedForums.entrySet());

//        for (Map.Entry<String, Integer> entry: updatedForums.entrySet()) {
//            updatedForumsList.add(entry);
//        }

        try {
            jdbc.batchUpdate(sqlUpdateForum, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                    Map.Entry entry = updatedForumsList.get(i);
                    preparedStatement.setInt(1, (Integer) entry.getValue());
                    preparedStatement.setString(2, (String) entry.getKey());
                }

                @Override
                public int getBatchSize() {
                    return updatedForumsList.size();
                }
            });
        } catch (DataAccessException e) {
            return null;
        }

        return newPosts;
    }

    // Done
    public List<Post> getPostList(Integer id, Integer limit, Integer since, String sort, Boolean desc) {
        Integer number = 0;

        if (since == null && !desc) number = 0;
        if (since == null && desc)  number = 1;
        if (since != null && !desc) number = 2;
        if (since != null && desc)  number = 3;

        String sql = queries.get(sort)[number];

        List<Post> postList;
        try {
            if (since == null)
                postList = jdbc.query(sql, new postMapper(), id, limit);
            else
                postList = jdbc.query(sql, new postMapper(), id, since, limit);
        } catch (DataAccessException e) {
            return null;
        }
        return postList;
    }

    private final class postMapper implements RowMapper<Post> {
        @Override
        public Post mapRow(ResultSet rs, int rowNum) throws SQLException {
            final String author = rs.getString("author");
            final String created = rs.getTimestamp("created").toInstant().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            final String forum = rs.getString("forum");
            final Integer id = rs.getInt("id");
            final Boolean isEdited = rs.getBoolean("isEdited");
            final String message = rs.getString("message");
            final Integer parent = rs.getInt("parent");
            final Integer thread = rs.getInt("thread");
            return new Post(author, created, forum, id, isEdited, message, parent, thread);
        }
    }
}
