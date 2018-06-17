package forumapi.databases.messages;

public enum MessageStates {
    USER_NOT_FOUND("Can't find user by nickname: "),
    EMAIL_ALREADY_REGISTERED("This email is already registered by user: "),
    FORUM_NOT_FOUND("Can't find forum by slug: "),
    THREAD_NOT_FOUND("Can't find thread by slug or id: "),
    POST_NOT_FOUND("Can't find post by id: "),
    POSTS_CONFLICT("Parent post was created in another thread. "),
    CLEAR_SUCCESSFUL("Очистка прошла успешно. "),
    USER_IN_FORUM_NOT_FOUND("Can't find users in forum: ");



    private String message;

    MessageStates(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
