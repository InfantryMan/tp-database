package forumapi.databases.services;

import forumapi.databases.models.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ApplicationService {
     private final UserService userService;
     private final PostService postService;
     private final VoteService voteService;
     private final ForumService forumService;
     private final  ThreadService threadService;

    @Autowired
    public ApplicationService(UserService userService,
                              PostService postService,
                              ForumService forumService,
                              ThreadService threadService,
                              VoteService voteService) {
        this.userService = userService;
        this.postService = postService;
        this.forumService = forumService;
        this.threadService = threadService;
        this.voteService =  voteService;
    }

    public Status getStatus(){
        return new Status(userService.getCount(), forumService.getCount(), threadService.getCount(), postService.getCount());
    }

    public void truncateTables() {
        voteService.truncateTable();
        postService.truncateTable();
        threadService.truncateTable();
        forumService.truncateTable();
        userService.truncateTable();
    }
}