package org.training.chat.data;

import io.vertx.core.json.Json;

import java.util.Objects;

/**
 * Универсальный POJO объект
 * Необходимый для передачи разлиных сообщений от RouterMethod до конкретных обрабатывающих Verticle
 *
 * @param <T> JSON полученный от пользователя
 */
public class GenericMessage<T> {
    private UserDto author;
    private T message;
    private Long timestamp;

    public GenericMessage() {
    }

    public GenericMessage(UserDto author, T message, Long timestamp) {
        this.author = author;
        this.message = message;
        this.timestamp = timestamp;
    }

    public UserDto getAuthor() {
        return author;
    }

    public void setAuthor(UserDto author) {
        this.author = author;
    }

    public T getMessage() {
        return message;
    }

    public void setMessage(T message) {
        this.message = message;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return Json.encode(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenericMessage<?> that = (GenericMessage<?>) o;
        return Objects.equals(author, that.author) &&
                Objects.equals(message, that.message) &&
                Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {

        return Objects.hash(author, message, timestamp);
    }
}
