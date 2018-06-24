package forumapi.databases.services;

import forumapi.databases.models.Queries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VoteService {
    private final JdbcTemplate jdbc;

    @Autowired
    public VoteService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void truncateTable() {
        jdbc.execute(Queries.truncateVotes);
    }
}
