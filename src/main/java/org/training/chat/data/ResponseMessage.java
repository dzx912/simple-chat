package org.training.chat.data;

import io.vertx.core.json.JsonObject;

/**
 * POJO объект для ответов клиенту
 */
public class ResponseMessage {
    private String type;
    private JsonObject content;

    public ResponseMessage() {
    }

    public ResponseMessage(String type, JsonObject content) {
        this.type = type;
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public JsonObject getContent() {
        return content;
    }

    public void setContent(JsonObject content) {
        this.content = content;
    }
}
