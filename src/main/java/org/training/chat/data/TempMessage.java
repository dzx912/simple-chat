package org.training.chat.data;

/**
 * POJO, необходимый для передачи временных данных
 * Чтобы потом сформировать TextMessage
 */
public class TempMessage {

    private String token;
    private String message;

    public TempMessage() {
    }

    public TempMessage(String token, String message) {
        this.token = token;
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
