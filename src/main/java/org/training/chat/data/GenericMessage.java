package org.training.chat.data;

import io.vertx.core.json.Json;

/**
 * Универсальный POJO объект
 * Необходимый для передачи разлиных сообщений от RouterMethod до конкретных обрабатывающих Verticle
 *
 * @param <T> JSON полученный от пользователя
 */
public record GenericMessage<T>(UserDto author, T message, Long timestamp) {
    @Override
    public String toString() {
        return Json.encode(this);
    }
}
