package forumapi.databases.controllers;

import forumapi.databases.messages.Message;
import forumapi.databases.messages.MessageStates;
import forumapi.databases.models.*;
import forumapi.databases.models.Thread;
import forumapi.databases.services.ForumService;
import forumapi.databases.services.PostService;
import forumapi.databases.services.ThreadService;
import forumapi.databases.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping(path = "/api/post")
public class PostController {
    final private ForumService forumService;
    final private UserService userService;
    final private PostService postService;
    final private ThreadService threadService;

    @Autowired
    public PostController(ForumService forumService, UserService userService, PostService postService, ThreadService threadService) {
        this.forumService = forumService;
        this.userService = userService;
        this.postService = postService;
        this.threadService = threadService;
    }

    // Done
    @RequestMapping(path = "/{id}/details", method = RequestMethod.POST)
    public ResponseEntity updatePost(@PathVariable(name = "id") Integer id,
                                     @RequestBody PostUpdate bodyPostUpdate) {
        Post post = postService.updatePost(bodyPostUpdate, id);

        if (post == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(MessageStates.POST_NOT_FOUND.getMessage() + id));
        }

        return ResponseEntity.status(HttpStatus.OK).body(post);
    }

    // Done
    @RequestMapping(path = "/{id}/details", method = RequestMethod.GET)
    public ResponseEntity detailsPost(@PathVariable(name = "id") Integer id,
                                      @RequestParam(name = "related", required = false) List<String> related) {
        final Post post = postService.getPostById(id);

        if (post == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(MessageStates.POST_NOT_FOUND.getMessage() + id.toString()));
        }

        if (related == null || related.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(new PostFull(post));
        }

        User user = null;
        Forum forum = null;
        Thread thread = null;

        for (String entity : related) {
            switch (entity) {
                case ("user"):
                    user = userService.getUserByNickName(post.getAuthor());
                    break;
                case ("thread"):
                    thread = threadService.getThreadById(post.getThread());
                    break;
                case ("forum"):
                    forum = forumService.getForumBySlug(post.getForum());
                    break;
                default:
                    break;
            }
        }

        return ResponseEntity.status(HttpStatus.OK).body(new PostFull(post, user, thread, forum));
    }
}
