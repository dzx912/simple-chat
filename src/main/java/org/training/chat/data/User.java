package org.training.chat.data;

import io.vertx.core.json.Json;

/**
 * POJO объект, хранящий информацию о пользователях
 * Передается как вложенный объект в отправляемом сообщении
 */
public record User(String id, String login, String firstName, String lastName) {
    @Override
    public String toString() {
        return Json.encode(this);
    }
}
