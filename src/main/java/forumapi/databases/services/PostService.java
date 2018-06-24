package forumapi.databases.services;

import ch.qos.logback.classic.db.SQLBuilder;
import forumapi.databases.models.Post;
import forumapi.databases.models.PostUpdate;
import forumapi.databases.models.Queries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.crypto.Data;
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

    @Autowired
    public PostService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Integer getCount() {
        try {
            return jdbc.queryForObject(Queries.selectPostsCount, Integer.class);
        } catch (DataAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void truncateTable() {
        try {
            jdbc.execute(Queries.truncatePosts);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }

    public Post getPostById(Integer id) {
        Post post = null;
        try {
            post = jdbc.queryForObject(Queries.selectPostById, new postMapper(), id);
        } catch (DataAccessException e) {
            return null;
        }
        return post;
    }

    public Post updatePost(PostUpdate postUpdate, Integer id) {
        Post oldPost = getPostById(id);

        if (oldPost == null) {
            return null;
        }

        if (postUpdate.getMessage() == null || oldPost.getMessage().equals(postUpdate.getMessage())) {
            return oldPost;
        }

        Post post = null;
        try {
            post = jdbc.queryForObject(Queries.updatePost, new postMapper(), postUpdate.getMessage(), id);
        } catch (DataAccessException e) {
            return null;
        }
        return post;
    }

    public List<Post> addPostList(List<Post> posts) {

        final Timestamp curr_time = jdbc.queryForObject(Queries.selectCurrentTime, Timestamp.class);
        final String time = curr_time.toInstant().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        try {
            jdbc.batchUpdate(Queries.insertPost, new postBatchPreparedStatementSetter(posts, time, curr_time));
            jdbc.batchUpdate(Queries.insertToUserForum, new forumUserBatchPreparedStatementSetter(posts));
        } catch (DataAccessException e) {
            e.printStackTrace();
            return null;
        }

        try {
            jdbc.update(Queries.updateForumPosts, posts.size(), posts.get(0).getForum());
        } catch (DataAccessException e) {
            e.printStackTrace();
            return null;
        }

        return posts;
    }

    public List<Post> execSortQuery(String sql, Integer id, Integer limit, Integer since) {
        List<Post> postList = null;
        try {
            if (since == null) {
                postList = jdbc.query(sql, new postMapper(), id, limit);
            } else {
                postList = jdbc.query(sql, new postMapper(), id, since, limit);
            }
        } catch (DataAccessException e) {
            return null;
        }
        return postList;
    }

    public List<Post> flatSort(Integer id, Integer limit, Integer since, Boolean desc) {
        String sql = null;
        if (since == null && !desc) { sql = Queries.flatSort; }
        if (since == null && desc)  { sql = Queries.flatSortDesc; }
        if (since != null && !desc) { sql = Queries.flatSortSince; }
        if (since != null && desc)  { sql = Queries.flatSortDescSince; }

        List<Post> postList = execSortQuery(sql, id, limit, since);
        return postList;
    }

    public List<Post> treeSort(Integer id, Integer limit, Integer since, Boolean desc) {
        String sql = null;
        if (since == null && !desc) { sql = Queries.treeSort; }
        if (since == null && desc)  { sql = Queries.treeSortDesc; }
        if (since != null && !desc) { sql = Queries.treeSortSince; }
        if (since != null && desc)  { sql = Queries.treeSortDescSince; }

        List<Post> postList = execSortQuery(sql, id, limit, since);
        return postList;
    }

    public List<Post> parentTreeSort (Integer id, Integer limit, Integer since, Boolean desc) {

        StringBuilder sqlBuilderParent = new StringBuilder(Queries.parentTreeGetParents);
        String order = (desc) ? "DESC ": "ASC ";

        List<Integer> parents = null;
        try {
            if (since != null) {
                String sign = (!desc) ? "> ": "< ";
                sqlBuilderParent.append("AND post_path[1] " + sign);
                Integer postPathSince = jdbc.queryForObject(Queries.parentTreeGetParentsSince, Integer.class, since);
                sqlBuilderParent.append(postPathSince.toString()+ " ");
            }
            sqlBuilderParent.append("ORDER BY id " + order + "LIMIT ?;");
            String sqlParent = sqlBuilderParent.toString();
            parents = jdbc.queryForList(sqlParent, Integer.class, id, limit);
        } catch (DataAccessException e) {
            e.printStackTrace();
            return null;
        }

        if (parents.isEmpty()) {
            return new ArrayList<>();
        }

        String parentString = parents.toString();
        StringBuilder parentStringBuilder = new StringBuilder(parentString);
        parentStringBuilder.deleteCharAt(0);
        parentStringBuilder.deleteCharAt(parentStringBuilder.length() - 1);
        parentStringBuilder.replace(0, 0, "(") ;
        parentStringBuilder.replace(parentString.length() - 1, parentString.length() - 1, ")" );
        parentString = parentStringBuilder.toString();

        List<Post> resultPosts = null;
        try {
            StringBuilder sqlBuilder = new StringBuilder(Queries.parentTree);
            sqlBuilder.append(parentString + " ");
            sqlBuilder.append("ORDER BY post_path[1] " + order + ", post_path;");
            String sql = sqlBuilder.toString();
            resultPosts = jdbc.query(sql, new postMapper());
        } catch (DataAccessException e) {
            e.printStackTrace();
            return null;
        }

        return resultPosts;
    }

    public List<Post> getPostList(Integer id, Integer limit, Integer since, String sort, Boolean desc) {
        List<Post> postList = null;
        switch(sort) {
            case("flat"):
                postList = flatSort(id, limit, since, desc);
                break;
            case("tree"):
                postList = treeSort(id, limit, since, desc);
                break;
            case("parent_tree"):
                postList = parentTreeSort(id, limit, since, desc);
                break;
            default:
                break;
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

    private final class postBatchPreparedStatementSetter implements BatchPreparedStatementSetter {
        List<Post> posts;
        String time;
        Timestamp curr_time;

        public postBatchPreparedStatementSetter(List<Post> posts, String time, Timestamp curr_time) {
            this.posts = posts;
            this.time = time;
            this.curr_time = curr_time;
        }

        @Override
        public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
            Post post = posts.get(i);
            Integer seq = null;
            try {
                seq = jdbc.queryForObject(Queries.selectPostSeq, Integer.class);
            } catch (DataAccessException e) {
                e.printStackTrace();
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
            preparedStatement.setInt(8, post.getParent());
            preparedStatement.setInt(9, post.getId());
        }
        @Override
        public int getBatchSize() {
            return posts.size();
        }

    }

    private final class forumUserBatchPreparedStatementSetter implements BatchPreparedStatementSetter {
        List<Post> posts;

        public forumUserBatchPreparedStatementSetter(List<Post> posts) {
            this.posts = posts;
        }

        @Override
        public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
            Post post = posts.get(i);
            preparedStatement.setString(1, post.getForum());
            preparedStatement.setString(2, post.getAuthor());
        }
        @Override
        public int getBatchSize() {
            return posts.size();
        }

    }
}
