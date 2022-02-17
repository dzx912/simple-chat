package org.training.chat.data;

import io.vertx.core.json.Json;

/**
 * POJO объект для внутренней комунникации, чтобы сообщить, что чат создан
 */
public record ResponseCreateChat(User author, Chat chat) {
    @Override
    public String toString() {
        return Json.encode(this);
    }
}
