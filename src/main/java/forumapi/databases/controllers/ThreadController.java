package forumapi.databases.controllers;

import forumapi.databases.messages.Message;
import forumapi.databases.messages.MessageStates;
import forumapi.databases.models.Post;
import forumapi.databases.models.Thread;
import forumapi.databases.models.Vote;
import forumapi.databases.services.ForumService;
import forumapi.databases.services.PostService;
import forumapi.databases.services.ThreadService;
import forumapi.databases.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "/api/thread")
public class ThreadController {
    private final UserService userService;
    private final ForumService forumService;
    private final ThreadService threadService;
    private final PostService postService;

    @Autowired
    public ThreadController(UserService userService, ForumService forumService, ThreadService threadService, PostService postService) {
        this.userService = userService;
        this.forumService = forumService;
        this.threadService = threadService;
        this.postService = postService;
    }

    // Done
    @RequestMapping(path = "/{slug_or_id}/create", method = RequestMethod.POST)
    public ResponseEntity createPost(@PathVariable("slug_or_id") String slug_or_id,
                                     @RequestBody List<Post> bodyPostList) {

        Thread thread = threadService.getThreadBySlugOrId(slug_or_id);

        if (thread == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message((MessageStates.THREAD_NOT_FOUND.getMessage()) + slug_or_id));
        }

        if (bodyPostList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(new ArrayList<>());
        }

        for (Post post: bodyPostList){
            Integer parentId = post.getParent();
            if (parentId != null) {
                Post parent = postService.getPostById(post.getParent());
                if (parent == null) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(new Message(MessageStates.POSTS_CONFLICT.getMessage()));
                }
                Integer threadId = thread.getId();
                Integer parentThreadId = parent.getThread();
                if (!threadId.equals(parentThreadId) ) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(new Message(MessageStates.POSTS_CONFLICT.getMessage()));
                }
            }
            post.setForum(thread.getForum());
            post.setThread(thread.getId());
            if (parentId == null) {
                post.setParent(0);
            }
            String nickname = post.getAuthor();
            if (userService.getUserByNickName(nickname) == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(MessageStates.USER_NOT_FOUND.getMessage() + nickname));
            }
        }

        List<Post> newPosts = null;
        newPosts = postService.addPostList(bodyPostList);
        return ResponseEntity.status(HttpStatus.CREATED).body(newPosts);
    }

    // Done
    @RequestMapping(path = "/{slug_or_id}/details", method = RequestMethod.GET)
    public ResponseEntity detailsThread(@PathVariable("slug_or_id") String slug_or_id) {

        Thread thread = threadService.getThreadBySlugOrId(slug_or_id);

        if (thread == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(MessageStates.THREAD_NOT_FOUND.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.OK).body(thread);
    }

    // Done
    @RequestMapping(path = "/{slug_or_id}/posts", method = RequestMethod.GET)
    public ResponseEntity getPosts(@PathVariable("slug_or_id") String slug_or_id,
                                   @RequestParam(value = "limit", required = false, defaultValue = "100") Integer limit,
                                   @RequestParam(value = "since", required = false) Integer since,
                                   @RequestParam(value = "sort", required = false, defaultValue = "flat") String sort,
                                   @RequestParam(value = "desc", required = false, defaultValue = "false") Boolean desc) {

        Thread thread = threadService.getThreadBySlugOrId(slug_or_id);

        if (thread == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(MessageStates.THREAD_NOT_FOUND.getMessage()));
        }

        List<Post> postList = postService.getPostList(thread.getId(), limit, since, sort, desc);

        return ResponseEntity.status(HttpStatus.OK).body(postList);
    }

    // Done
    @RequestMapping(path = "/{slug_or_id}/vote", method = RequestMethod.POST)
    public ResponseEntity changeVote(@PathVariable("slug_or_id") String slug_or_id,
                                     @RequestBody Vote bodyVote) {

        Thread thread = threadService.getThreadBySlugOrId(slug_or_id);

        if (thread == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(MessageStates.THREAD_NOT_FOUND.getMessage() + slug_or_id));
        }

        String nickname = bodyVote.getNickname();
        if (userService.getUserByNickName(nickname) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(MessageStates.USER_NOT_FOUND.getMessage() + nickname));
        }

        Thread newThread = threadService.changeVote(thread, bodyVote);

        return ResponseEntity.status(HttpStatus.OK).body(newThread);
    }

    @RequestMapping(path = "/{slug_or_id}/details", method = RequestMethod.POST)
    public ResponseEntity updateThread(@PathVariable("slug_or_id") String slug_or_id,
                                       @RequestBody Thread bodyThread) {

        Thread thread = threadService.getThreadBySlugOrId(slug_or_id);

        if (thread == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(MessageStates.THREAD_NOT_FOUND.getMessage() + slug_or_id));
        }

        bodyThread.setSlug(thread.getSlug());
        bodyThread.setId(thread.getId());

        Thread newThread = threadService.updateThread(bodyThread);

        return ResponseEntity.status(HttpStatus.OK).body(newThread);
    }

}
