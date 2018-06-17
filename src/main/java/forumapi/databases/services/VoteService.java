package forumapi.databases.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VoteService {
    private final JdbcTemplate jdbc;
    private final ForumService forumService;
    private final PostService postService;
    private final ThreadService threadService;
    private final UserService userService;

    @Autowired
    public VoteService(JdbcTemplate jdbc,
                       ForumService forumService,
                       PostService postService,
                       ThreadService threadService,
                       UserService userService) {
        this.jdbc = jdbc;
        this.forumService = forumService;
        this.postService = postService;
        this.threadService = threadService;
        this.userService = userService;
    }

public void truncateTable() {
        String sql = "TRUNCATE TABLE votes CASCADE ;";
        jdbc.execute(sql);
    }
}
