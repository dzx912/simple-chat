package org.training.chat.util;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.training.chat.data.ResponseAcknowledge;
import org.training.chat.data.ResponseMessage;

/**
 * Класс, который генерирует Response клиенту
 */
public class Answerer {
    private Answerer() {
    }

    public static <T> String createResponseMessage(String type, T response) {
        String jsonText = Json.encode(response);
        JsonObject context = new JsonObject(jsonText);
        ResponseMessage responseMessage = new ResponseMessage(type, context);
        return Json.encode(responseMessage);
    }

    public static <T> String createResponseAcknowledge(String type, String method, T response) {
        String jsonText = Json.encode(response);
        JsonObject context = new JsonObject(jsonText);
        ResponseAcknowledge responseAcknowledge = new ResponseAcknowledge(type, context, method);
        return Json.encode(responseAcknowledge);
    }
}
