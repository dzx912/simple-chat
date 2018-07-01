package org.training.chat.data;

import io.vertx.core.json.Json;

import java.util.Objects;

/**
 * Общее сообщение, с частью, полученной от клиента
 * И добавленными метаданными на сервере
 */
public class TextMessage {

    private UserDto author;
    private String chatId;
    private String text;
    private Long clientId;
    private Long timestamp;

    public TextMessage() {
    }

    public TextMessage(UserDto author, String chatId, String text, Long clientId, Long timestamp) {
        this.author = author;
        this.chatId = chatId;
        this.text = text;
        this.clientId = clientId;
        this.timestamp = timestamp;
    }

    public UserDto getAuthor() {
        return author;
    }

    public void setAuthor(UserDto author) {
        this.author = author;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
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
        TextMessage that = (TextMessage) o;
        return Objects.equals(author, that.author) &&
                Objects.equals(chatId, that.chatId) &&
                Objects.equals(text, that.text) &&
                Objects.equals(clientId, that.clientId) &&
                Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {

        return Objects.hash(author, chatId, text, clientId, timestamp);
    }
}
