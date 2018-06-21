package forumapi.databases.services;

import forumapi.databases.models.Thread;
import forumapi.databases.models.Vote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Service
@Transactional
public class ThreadService {
    private final JdbcTemplate jdbc;
    private final String sqlCountThreads = "SELECT COUNT(id) FROM threads;";

    @Autowired
    public ThreadService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Integer getCount() {
        return jdbc.queryForObject(sqlCountThreads, Integer.class);
    }

    public void truncateTable() {
        final String sql = "TRUNCATE TABLE threads CASCADE ;";
        jdbc.execute(sql);
    }

    // Done
    public Thread addThread(Thread thread) {
        Thread newThread = null;
        final String sqlInsertThread = "INSERT INTO threads (author, forum, message, title) VALUES (?, ?, ?, ?) RETURNING id;";
        final String sqlUpdateThreadsInForum = "UPDATE forum SET threads = threads + 1 WHERE slug = ?;";
        final String sqlUpdateCreatedInThread = "UPDATE threads SET created = ? WHERE id = ? ;";
        final String sqlUpdateSlugInThread = "UPDATE threads SET slug = ? WHERE id = ?" ;
        final String sqlSelectUserForum = "SELECT COUNT(*) FROM users_forum " +
                "WHERE lower(author) = lower(?) AND lower(forum) = lower(?) " +
                "LIMIT 1; ";
        final String sqlInsertUserForum = "INSERT INTO users_forum(forum, author) VALUES (?, ?); ";
        try {
            Integer id = jdbc.queryForObject(sqlInsertThread, Integer.class, thread.getAuthor(), thread.getForum(), thread.getMessage(), thread.getTitle());
            jdbc.update(sqlUpdateThreadsInForum, thread.getForum());
            Integer isUserForum = jdbc.queryForObject(sqlSelectUserForum, Integer.class, thread.getAuthor(), thread.getForum());
            if (isUserForum == 0) {
                jdbc.update(sqlInsertUserForum, thread.getForum(), thread.getAuthor());
            }
            if (thread.getCreated() != null) {
                String str = ZonedDateTime.parse(thread.getCreated()).format(DateTimeFormatter.ISO_INSTANT);
                jdbc.update(sqlUpdateCreatedInThread, new Timestamp(ZonedDateTime.parse(str).toLocalDateTime().toInstant(ZoneOffset.UTC).toEpochMilli()), id );
            }
            if(thread.getSlug() != null) {
                jdbc.update(sqlUpdateSlugInThread, thread.getSlug(), id);
            }
            newThread = getThreadById(id);
        } catch (DataAccessException e) {
            return null;
        }
        return newThread;
    }

    // Done
    public Thread getThreadBySlugOrId(String slug_or_id) {
        if (slug_or_id.matches("[-+]?[0-9]+(\\.[0-9]+)?")) {
            return getThreadById(Integer.valueOf(slug_or_id));
        }
        return getThreadBySlug(slug_or_id);
    }

    // Done
    public Thread getThreadBySlug(String slug) {
        final String sql = "SELECT * FROM threads WHERE LOWER(slug) = LOWER(?);";
        Thread thread = null;
        try {
            thread = jdbc.queryForObject(sql, new threadMapper(), slug);
        } catch (DataAccessException e) {
            return null;
        }
        return thread;
    }

    // Done
    public Thread getThreadById(Integer id) {
        final String sql = "SELECT * FROM threads WHERE id = ?;";
        Thread thread = null;
        try {
            thread = jdbc.queryForObject(sql, new threadMapper(), id);
        } catch (DataAccessException e) {
            return null;
        }
        return thread;
    }

    // Done
    public Thread getDuplicateThread(Thread thread) {
        final String sql = "SELECT * FROM threads WHERE LOWER(slug) = LOWER(?);";
        Thread dupThread = null;
        try {
            dupThread = jdbc.queryForObject(sql, new threadMapper(), thread.getSlug());
        } catch (DataAccessException e) {
            return null;
        }
        return dupThread;
    }

    // Done
    public List<Thread> getThreadsByForumSlug(String forumSlug, Integer limit, String since, Boolean desc) {
        final String [] queries = {
                "SELECT * FROM threads WHERE LOWER(forum) = LOWER(?) ORDER BY created ASC LIMIT ?",
                "SELECT * FROM threads WHERE LOWER(forum) = LOWER(?) ORDER BY created DESC LIMIT ?",
                "SELECT * FROM threads WHERE LOWER(forum) = LOWER(?) AND created >= ? ORDER BY created ASC LIMIT ?",
                "SELECT * FROM threads WHERE LOWER(forum) = LOWER(?) AND created <= ? ORDER BY created DESC LIMIT ?"
        };

        Timestamp time = null;

        if (since != null) {
            final String str = ZonedDateTime.parse(since).format(DateTimeFormatter.ISO_INSTANT);
            time = new Timestamp(ZonedDateTime.parse(str).toLocalDateTime().toInstant(ZoneOffset.UTC).toEpochMilli());
        }

        String sql = null;
        if (since == null && !desc) sql = queries[0];
        if (since == null && desc)  sql = queries[1];
        if (since != null && !desc) sql = queries[2];
        if (since != null && desc)  sql = queries[3];

        List<Thread> threads = null;
        try {
            if (since != null)
                threads = jdbc.query(sql, new threadMapper(), forumSlug, time, limit);
            else
                threads = jdbc.query(sql, new threadMapper(), forumSlug, limit);
        } catch (DataAccessException e) {
            return null;
        }
        return threads;
    }

    // Done
    public Thread changeVote(Thread thread, Vote vote) {
        Thread returnThread = null;
        String sqlSelect = "SELECT voice FROM votes WHERE thread = ? AND nickname = ?;";
        Integer voiceDB;
        Integer voiceClient = vote.getVoice();
        Integer totalVoice = 0;

        try {
            voiceDB = jdbc.queryForObject(sqlSelect, Integer.class, thread.getId(), vote.getNickname());
        } catch (DataAccessException e) {
            voiceDB = 0;
        }

        if ( voiceClient.equals(voiceDB) ) {
            return thread;
        }

        if (voiceClient == 1 && voiceDB == -1) {
            totalVoice = 2;
        }

        if (voiceClient == 1 && voiceDB == 0) {
            totalVoice = 1;
        }

        if (voiceClient == -1 && voiceDB == 1) {
            totalVoice = -2;
        }

        if (voiceClient == -1 && voiceDB == 0) {
            totalVoice = -1;
        }

        String sqlUpdateThread = "UPDATE threads SET votes = votes + ? WHERE id = ?;";
        try {
            jdbc.update(sqlUpdateThread, totalVoice, thread.getId());
        } catch (DataAccessException e) {
            return null;
        }

        if (voiceDB == 0) {
            String sqlInsert = "INSERT INTO votes VALUES (?, ?, ?);";
            try {
                jdbc.update(sqlInsert, vote.getNickname(), voiceClient, thread.getId());
            } catch (DataAccessException e) {
                return null;
            }
        }

        if (totalVoice == 2 || totalVoice == -2) {
            String sqlUpdate = "UPDATE votes SET voice = ? WHERE thread = ? AND nickname = ?;";
            try {
                jdbc.update(sqlUpdate, voiceClient, thread.getId(), vote.getNickname());
            } catch (DataAccessException e) {
                return null;
            }
        }

        String sqlSelectThread = "SELECT * FROM threads WHERE id = ?;";
        try {
            returnThread = jdbc.queryForObject(sqlSelectThread, new threadMapper(), thread.getId());
        } catch (DataAccessException e) {
            return null;
        }

        return returnThread;
    }

    // Done
    public Thread updateThread(Thread thread) {
        StringBuilder sqlUpdateBuilder = new StringBuilder("UPDATE threads SET ");

        String newThreadMessage = thread.getMessage();
        String newThreadTitle = thread.getTitle();

        Boolean isUpdate = false;

        if (newThreadMessage != null) {
            sqlUpdateBuilder.append("message = \'").append(newThreadMessage).append("\', ");
            isUpdate = true;
        }

        if (newThreadTitle != null) {
            sqlUpdateBuilder.append("title = \'").append(newThreadTitle).append("\', ");
            isUpdate = true;
        }

        sqlUpdateBuilder.deleteCharAt(sqlUpdateBuilder.toString().length() - 2);
        sqlUpdateBuilder.append("WHERE id = ").append(thread.getId()).append(" RETURNING *;");

        Thread returnThread;
        if (isUpdate) {
            try {
                String sql = sqlUpdateBuilder.toString();
                returnThread = jdbc.queryForObject(sql, new threadMapper());
            } catch (DuplicateKeyException e) {
                return null;
            }
        } else {
            returnThread = getThreadById(thread.getId());
        }
        return returnThread;
    }

    private final class threadMapper implements RowMapper<Thread> {
        @Override
        public Thread mapRow(ResultSet rs, int rowNum) throws SQLException {
            final Integer id = rs.getInt("id");
            final String title = rs.getString("title");
            final String author = rs.getString("author");
            final String forum = rs.getString("forum");
            final String message = rs.getString("message");
            final Integer votes = rs.getInt("votes");
            final String slug = rs.getString("slug");
            final String created = rs.getTimestamp("created").toInstant().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            return new Thread(author, created, forum, id, message, slug, title, votes);
        }
    }
}
