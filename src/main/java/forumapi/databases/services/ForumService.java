package forumapi.databases.services;

import forumapi.databases.models.Forum;
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
    private final String sqlCountForums = "SELECT COUNT (slug) FROM forum;";

    @Autowired
    public ForumService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Integer getCount() {
        return jdbc.queryForObject(sqlCountForums, Integer.class);
    }

    public void truncateTable() {
        String sql = "TRUNCATE TABLE forum CASCADE ;";
        jdbc.execute(sql);
    }

    public void addForum(Forum forum) {
        String sql = "INSERT INTO forum (admin, title, slug) VALUES (?, ?, ?)";
        try {
            jdbc.update(sql,
                    ps -> {
                        ps.setString(1, forum.getUser());
                        ps.setString(2, forum.getTitle());
                        ps.setString(3, forum.getSlug());
                    });
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }

    // Done
    public Forum getForumBySlug(String slug) {
        final String sql = "SELECT * FROM forum WHERE LOWER(slug) = LOWER(?);";
        Forum forum = null;
        try {
            forum = jdbc.queryForObject(sql, new forumMapper(), slug);
        } catch(EmptyResultDataAccessException e) {
            return null;
        } catch (DataAccessException e) {
            e.printStackTrace();
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
