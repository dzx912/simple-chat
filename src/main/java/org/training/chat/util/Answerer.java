package org.training.chat.util;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.training.chat.data.ResponseMessage;

/**
 * Класс, который генерирует Response клиенту
 */
public class Answerer {
    private Answerer() {
    }

    public static <T> String createResponse(String type, T response) {
        String jsonText = Json.encode(response);
        JsonObject context = new JsonObject(jsonText);
        ResponseMessage responseMessage = new ResponseMessage(type, context);
        return Json.encode(responseMessage);
    }
}
