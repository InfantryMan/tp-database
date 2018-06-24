package forumapi.databases.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


public class Status {
    @JsonProperty
    private Integer user;
    @JsonProperty
    private Integer forum;
    @JsonProperty
    private Integer thread;
    @JsonProperty
    private Integer post;


    @JsonCreator
    public Status(@JsonProperty("user") Integer user, @JsonProperty("forum") Integer forum,
                  @JsonProperty("thread") Integer thread, @JsonProperty("post") Integer post){
        this.user = user;
        this.forum = forum;
        this.thread = thread;
        this.post = post;
    }

    public Integer getUser() {
        return user;
    }

    public Integer getForum() {
        return forum;
    }

    public Integer getThread() {
        return thread;
    }

    public Integer getPost() {
        return post;
    }

    public void setUser(Integer user) {
        this.user = user;
    }

    public void setForum(Integer forum) {
        this.forum = forum;
    }

    public void setThread(Integer thread) {
        this.thread = thread;
    }

    public void setPost(Integer post) {
        this.post = post;
    }
}
