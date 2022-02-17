package org.training.chat.data;

import io.vertx.core.json.JsonObject;

/**
 * POJO объект для ответов клиенту
 */
public record ResponseMessage(String type, JsonObject content) {
}
