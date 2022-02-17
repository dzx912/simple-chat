package org.training.chat.data;

import io.vertx.core.json.JsonObject;

/**
 * POJO объект для ответа о выполнении метода
 */
public record ResponseAcknowledge(String type, JsonObject content, String method) {
}
