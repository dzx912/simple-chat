package org.training.chat.data;

import io.vertx.core.json.Json;

/**
 * POJO объект для внутренней комунникации, чтобы сообщить, что чат создан
 */
public class ResponseCreateChat {
    private UserDto author;
    private Chat chat;

    public ResponseCreateChat() {
    }

    public ResponseCreateChat(UserDto author, Chat chat) {
        this.author = author;
        this.chat = chat;
    }

    public UserDto getAuthor() {
        return author;
    }

    public void setAuthor(UserDto author) {
        this.author = author;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    @Override
    public String toString() {
        return Json.encode(this);
    }
}
