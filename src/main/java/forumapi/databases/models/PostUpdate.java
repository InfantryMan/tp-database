package forumapi.databases.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PostUpdate {
    @JsonProperty
    private String message;

    @JsonCreator
    public PostUpdate(@JsonProperty("message") String message){
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
