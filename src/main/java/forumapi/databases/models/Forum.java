package forumapi.databases.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Forum {
    @JsonProperty
    private Integer posts;
    @JsonProperty
    private String slug;
    @JsonProperty
    private Integer threads;
    @JsonProperty
    private String title;
    @JsonProperty
    private String user;

    @JsonCreator
    public Forum(@JsonProperty("posts")Integer posts, @JsonProperty("slug")String slug,
                 @JsonProperty("threads")Integer threads, @JsonProperty("title")String title,
                 @JsonProperty("user")String user) {
        this.posts = posts;
        this.slug = slug;
        this.threads = threads;
        this.title = title;
        this.user = user;
    }

    public Integer getPosts() {
        return posts;
    }

    public void setPosts(Integer posts) {
        this.posts = posts;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Integer getThreads() {
        return threads;
    }

    public void setThreads(Integer threads) {
        this.threads = threads;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

}
