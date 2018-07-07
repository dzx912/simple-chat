package org.training.chat.data;

import io.vertx.core.json.Json;

import java.util.Objects;

/**
 * POJO, необходимый для передачи временных данных
 * Чтобы потом сформировать окончательное передаваемое сообщение
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

    @Override
    public String toString() {
        return Json.encode(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TempMessage that = (TempMessage) o;
        return Objects.equals(user, that.user) &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {

        return Objects.hash(user, message);
    }
}
