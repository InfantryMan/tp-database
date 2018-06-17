package forumapi.databases.controllers;

import forumapi.databases.messages.Message;
import forumapi.databases.messages.MessageStates;
import forumapi.databases.models.Forum;
import forumapi.databases.models.Thread;
import forumapi.databases.models.User;
import forumapi.databases.services.ForumService;
import forumapi.databases.services.ThreadService;
import forumapi.databases.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/forum")
public class ForumContoller {
    private final ForumService forumService;
    private final UserService userService;
    private final ThreadService threadService;


    @Autowired
    public ForumContoller(ForumService forumService, UserService userService, ThreadService threadService) {
        this.forumService = forumService;
        this.userService = userService;
        this.threadService = threadService;
    }

    // Done
    @RequestMapping(path="/create", method = RequestMethod.POST)
    public ResponseEntity createForum(@RequestBody Forum forumBody) {

        final User admin = userService.getUserByNickName(forumBody.getUser());
        if (admin == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(MessageStates.USER_NOT_FOUND.getMessage() + forumBody.getUser()));

        forumBody.setUser(admin.getNickname());

        final Forum dubForum = forumService.getForumBySlug(forumBody.getSlug());
        if (dubForum != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(dubForum);
        }

        forumService.addForum(forumBody);

        Forum forum  = forumService.getForumBySlug(forumBody.getSlug());
        return ResponseEntity.status(HttpStatus.CREATED).body(forum);
    }

    // Done
    @RequestMapping(path="{slug}/details", method = RequestMethod.GET)
    public ResponseEntity showForum(@PathVariable("slug") String slug) {
        Forum forum = forumService.getForumBySlug(slug);
        if (forum == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(MessageStates.FORUM_NOT_FOUND.getMessage() + slug));
        }
        return ResponseEntity.status(HttpStatus.OK).body(forum);
    }

    // Done
    @RequestMapping(path="/{slug}/create", method = RequestMethod.POST)
    public ResponseEntity createThread(@PathVariable("slug") String forumSlug, @RequestBody Thread threadBody) {

        User user = userService.getUserByNickName(threadBody.getAuthor());
        if (user == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(MessageStates.USER_NOT_FOUND + threadBody.getAuthor()));

        Forum forum = forumService.getForumBySlug(forumSlug);
        if (forum == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(MessageStates.FORUM_NOT_FOUND + forumSlug));

        threadBody.setAuthor(user.getNickname());
        threadBody.setForum(forum.getSlug());

        Thread thread = threadService.getDuplicateThread(threadBody);
        if (thread != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(thread);
        }

        Thread newThread = threadService.addThread(threadBody);
        if(newThread == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(newThread);
    }

    // Done
    @RequestMapping(path="/{slug}/threads", method = RequestMethod.GET)
    public ResponseEntity showThreads(@PathVariable("slug") String forumSlug,
                                      @RequestParam(value = "limit", required = false) Integer limit,
                                      @RequestParam(value = "since", required = false) String since,
                                      @RequestParam(value = "desc", required = false, defaultValue = "false") Boolean desc) {
        Forum forum = forumService.getForumBySlug(forumSlug);
        if (forum == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(MessageStates.FORUM_NOT_FOUND.getMessage() + forumSlug));

        List<Thread> threadList = threadService.getThreadsByForumSlug(forumSlug, limit, since, desc);
        return ResponseEntity.status(HttpStatus.OK).body(threadList);
    }

    // Done
    @RequestMapping(path="/{slug}/users", method = RequestMethod.GET)
    public ResponseEntity showUsers(@PathVariable("slug") String forumSlug,
                                    @RequestParam(value = "limit", required = false) Integer limit,
                                    @RequestParam(value = "since", required = false) String since,
                                    @RequestParam(value = "desc", required = false, defaultValue = "false") Boolean desc) {

        Forum forum = forumService.getForumBySlug(forumSlug);

        if (forum == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(MessageStates.FORUM_NOT_FOUND.getMessage() + forumSlug));

        List<User> userList = userService.getUsersByForumSlug(forumSlug, limit, since, desc);
        if(userList == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(MessageStates.USER_IN_FORUM_NOT_FOUND.getMessage() + forumSlug));
        }

        return ResponseEntity.status(HttpStatus.OK).body(userList);
    }
}
