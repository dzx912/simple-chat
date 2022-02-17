package org.training.chat.data;

import io.vertx.core.json.Json;

/**
 * Общее сообщение, с частью, полученной от клиента
 * И добавленными метаданными на сервере
 */
public record TextMessage(UserDto author, String chatId, String text, Long clientId, Long timestamp) {
    @Override
    public String toString() {
        return Json.encode(this);
    }
}
