package forumapi.databases.services;

import forumapi.databases.models.Forum;
import forumapi.databases.models.Queries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;

@Service
@Transactional
public class ForumService {
    private final JdbcTemplate jdbc;

    @Autowired
    public ForumService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Integer getCount() {
        try {
            return jdbc.queryForObject(Queries.selectForumsCount, Integer.class);
        } catch (DataAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void truncateTable() {
        try {
            jdbc.execute(Queries.truncateForums);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }

    public void addForum(Forum forum) {
        try {
            jdbc.update(Queries.insertForum,
                    ps -> {
                        ps.setString(1, forum.getUser());
                        ps.setString(2, forum.getTitle());
                        ps.setString(3, forum.getSlug());
                    });
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }

    public Forum getForumBySlug(String slug) {
        Forum forum = null;
        try {
            forum = jdbc.queryForObject(Queries.selectForum, new forumMapper(), slug);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (DataAccessException e) {
            return null;
        }
        return forum;
    }

    private final class forumMapper implements RowMapper<Forum> {
        @Override
        public Forum mapRow(ResultSet rs, int rowNum) throws SQLException {
            final String userName = rs.getString("admin");
            final String title = rs.getString("title");
            final String slug = rs.getString("slug");
            final Integer posts = rs.getInt("posts");
            final Integer threads = rs.getInt("threads");

            return new Forum(posts, slug, threads, title, userName);
        }
    }

}
