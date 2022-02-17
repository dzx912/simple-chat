package org.training.chat.data;

import io.vertx.core.json.Json;

/**
 * POJO, необходимый для передачи временных данных
 * Чтобы потом сформировать окончательное передаваемое сообщение
 */
public record TempMessage(User user, String message) {
    @Override
    public String toString() {
        return Json.encode(this);
    }
}
