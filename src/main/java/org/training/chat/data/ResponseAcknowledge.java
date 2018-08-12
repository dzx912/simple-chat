package org.training.chat.data;

import io.vertx.core.json.JsonObject;

/**
 * POJO объект для ответа о выполнении метода
 */
public class ResponseAcknowledge extends ResponseMessage {
    private String method;

    public ResponseAcknowledge(String type, JsonObject content, String method) {
        super(type, content);
        this.method = method;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
