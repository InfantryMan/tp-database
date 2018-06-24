package forumapi.databases.services;

import forumapi.databases.models.Queries;
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

    @Autowired
    public ThreadService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Integer getCount() {
        try {
            return jdbc.queryForObject(Queries.selectThreadsCount, Integer.class);
        } catch (DataAccessException e) {
            return null;
        }
    }

    public void truncateTable() {
        try {
            jdbc.execute(Queries.truncateThreads);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }

    public Thread addThread(Thread thread) {
        Thread newThread = null;
        try {
            Integer id = jdbc.queryForObject(Queries.insertThread, Integer.class, thread.getAuthor(), thread.getForum(), thread.getMessage(), thread.getTitle());
            jdbc.update(Queries.updateForumThreads, thread.getForum());
            Integer isUserForum = jdbc.queryForObject(Queries.selectUserForumCount, Integer.class, thread.getAuthor(), thread.getForum());
            if (isUserForum == 0) {
                jdbc.update(Queries.insertToUserForum, thread.getForum(), thread.getAuthor());
            }
            if (thread.getCreated() != null) {
                String str = ZonedDateTime.parse(thread.getCreated()).format(DateTimeFormatter.ISO_INSTANT);
                jdbc.update(Queries.updateThreadCreated, new Timestamp(ZonedDateTime.parse(str).toLocalDateTime().toInstant(ZoneOffset.UTC).toEpochMilli()), id );
            }
            if(thread.getSlug() != null) {
                jdbc.update(Queries.updateThreadSlug, thread.getSlug(), id);
            }
            newThread = getThreadById(id);
        } catch (DataAccessException e) {
            e.printStackTrace();
            return null;
        }
        return newThread;
    }

    public Thread getThreadBySlugOrId(String slug_or_id) {
        if (slug_or_id.matches("[-+]?[0-9]+(\\.[0-9]+)?")) {
            return getThreadById(Integer.valueOf(slug_or_id));
        }
        return getThreadBySlug(slug_or_id);
    }

    public Thread getThreadBySlug(String slug) {
        Thread thread = null;
        try {
            thread = jdbc.queryForObject(Queries.selectThreadBySlug, new threadMapper(), slug);
        } catch (DataAccessException e) {
            return null;
        }
        return thread;
    }

    public Thread getThreadById(Integer id) {
        Thread thread = null;
        try {
            thread = jdbc.queryForObject(Queries.selectThreadById, new threadMapper(), id);
        } catch (DataAccessException e) {
            return null;
        }
        return thread;
    }

    public Thread getDuplicateThread(Thread thread) {
        Thread dupThread = null;
        try {
            dupThread = jdbc.queryForObject(Queries.selectThreadByForumSlug, new threadMapper(), thread.getSlug());
        } catch (DataAccessException e) {
            return null;
        }
        return dupThread;
    }

    public List<Thread> getThreadsByForumSlug(String forumSlug, Integer limit, String since, Boolean desc) {
        Timestamp time = null;
        if (since != null) {
            final String str = ZonedDateTime.parse(since).format(DateTimeFormatter.ISO_INSTANT);
            time = new Timestamp(ZonedDateTime.parse(str).toLocalDateTime().toInstant(ZoneOffset.UTC).toEpochMilli());
        }

        String sql = null;
        if (since == null && !desc) sql = Queries.selectThreadByForumSlugAsc;
        if (since == null && desc)  sql = Queries.selectThreadByForumSlugDesc;
        if (since != null && !desc) sql = Queries.selectThreadByForumSlugAscSince;
        if (since != null && desc)  sql = Queries.selectThreadByForumSlugDescSince;

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

    public Thread changeVote(Thread thread, Vote vote) {

        Integer voiceDB;
        Integer voiceClient = vote.getVoice();
        Integer totalVoice = 0;

        try {
            voiceDB = jdbc.queryForObject(Queries.selectVote, Integer.class, thread.getId(), vote.getNickname());
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

        try {
            jdbc.update(Queries.updateThreadVote, totalVoice, thread.getId());
        } catch (DataAccessException e) {
            return null;
        }

        if (voiceDB == 0) {
            try {
                jdbc.update(Queries.insertVote, vote.getNickname(), voiceClient, thread.getId());
            } catch (DataAccessException e) {
                return null;
            }
        }

        if (totalVoice == 2 || totalVoice == -2) {
            try {
                jdbc.update(Queries.updateVote, voiceClient, thread.getId(), vote.getNickname());
            } catch (DataAccessException e) {
                return null;
            }
        }

        Thread returnThread = getThreadById(thread.getId());

        return returnThread;
    }

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
