package forumapi.databases.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
    @JsonProperty
    private String about;
    @JsonProperty
    private String email;
    @JsonProperty
    private String fullname;
    @JsonProperty
    private String nickname;

    public User(@JsonProperty("about") String about,
                @JsonProperty("email") String email,
                @JsonProperty("fullname") String fullname,
                @JsonProperty("nickname") String nickname) {
        this.about = about;
        this.email = email;
        this.fullname = fullname;
        this.nickname = nickname;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Boolean checkUser() {
        if (nickname == null || about == null || email == null || fullname == null)
            return false;
        return true;
    }

    public Boolean isEmpty() {
        if (nickname == null && about == null && email == null && fullname == null)
            return true;
        return false;
    }
}
