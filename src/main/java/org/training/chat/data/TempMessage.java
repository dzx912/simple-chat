package org.training.chat.data;

/**
 * POJO, необходимый для передачи временных данных
 * Чтобы потом сформировать TextMessage
 */
public class TempMessage {

    private UserDto user;
    private String message;

    public TempMessage() {
    }

    public TempMessage(UserDto user, String message) {
        this.user = user;
        this.message = message;
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
