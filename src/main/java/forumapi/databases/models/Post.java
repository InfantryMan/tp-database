package forumapi.databases.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Post {
    @JsonProperty
    private String author;
    @JsonProperty
    private String created;
    @JsonProperty
    private String forum;
    @JsonProperty
    private Integer id;
    @JsonProperty
    private Boolean isEdited;
    @JsonProperty
    private String message;
    @JsonProperty
    private Integer parent;
    @JsonProperty
    private Integer thread;

    @JsonCreator
    public Post(@JsonProperty("author") String author, @JsonProperty("created") String created,
                @JsonProperty("forum") String forum, @JsonProperty("id") Integer id,
                @JsonProperty("isEdited") Boolean isEdited, @JsonProperty("message") String message,
                @JsonProperty("parent") Integer parent, @JsonProperty("thread") Integer thread) {
        this.author = author;
        this.created = created;
        this.forum = forum;
        this.id = id;
        this.isEdited = isEdited;
        this.message = message;
        this.parent = parent;
        this.thread = thread;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getForum() {
        return forum;
    }

    public void setForum(String forum) {
        this.forum = forum;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getEdited() {
        return isEdited;
    }

    public void setEdited(Boolean isEdited) {
        this.isEdited = isEdited;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getParent() {
        return parent;
    }

    public void setParent(Integer parent) {
        this.parent = parent;
    }

    public Integer getThread() {
        return thread;
    }

    public void setThread(Integer thread) {
        this.thread = thread;
    }
}
