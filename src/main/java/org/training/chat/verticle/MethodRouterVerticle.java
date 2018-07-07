package org.training.chat.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.training.chat.constants.BusEndpoints;
import org.training.chat.constants.WsMethod;
import org.training.chat.data.GenericMessage;
import org.training.chat.data.RequestTextMessage;
import org.training.chat.data.TempMessage;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.EnumMap;

import static org.training.chat.constants.BusEndpoints.*;

/**
 * Actor, который определяет в какие Actor'ы перенаправить входящий запрос
 * Выясняет это по полю "method" во входящем JSON
 */
public class MethodRouterVerticle extends AbstractVerticle {
    private static final String WEB_SOCKET_CLOSE = "\u0003�";
    private final Logger logger = LogManager.getLogger(MethodRouterVerticle.class);

    private static final EnumMap<WsMethod, BusEndpoints> WS_METHODS = new EnumMap<>(WsMethod.class);
    private static final EnumMap<WsMethod, Class> WS_METHODS_TO_CLASS = new EnumMap<>(WsMethod.class);

    static {
        WS_METHODS.put(WsMethod.sendTextMessage, ROUTER_CHAT);
        WS_METHODS.put(WsMethod.createChat, DB_CHAT_CREATE);

        WS_METHODS_TO_CLASS.put(WsMethod.sendTextMessage, RequestTextMessage.class);
    }

    @Override
    public void start() {
        logger.debug("Deploy " + MethodRouterVerticle.class);
        vertx.eventBus().localConsumer(ROUTER_METHOD.getPath(), this::routeMethod);
    }

    private void routeMethod(Message<TempMessage> data) {
        TempMessage tempMessage = data.body();
        try {
            String clientMessage = tempMessage.getMessage();
            JsonObject json = new JsonObject(clientMessage);

            boolean webSocketIsClosed = clientMessage.isEmpty() || WEB_SOCKET_CLOSE.equals(clientMessage);
            if (!webSocketIsClosed) {
                String method = json.getString("method");
                String path = getPath(method);
                GenericMessage genericMessage = createGenericMessage(tempMessage, json, method);
                vertx.eventBus().send(path, genericMessage);
                data.reply("ok");
            } else {
                data.fail(-1, "Empty client message");
            }
        } catch (DecodeException e) {
            data.fail(-2, "Cannot deserialize data: " + tempMessage);
        }
    }

    private String getPath(String method) {
        WsMethod wsMethod = WsMethod.valueOf(method);
        BusEndpoints busEndpoints = WS_METHODS.get(wsMethod);
        return busEndpoints.getPath();
    }

    private Class getRequestClass(String method) {
        WsMethod wsMethod = WsMethod.valueOf(method);
        return WS_METHODS_TO_CLASS.get(wsMethod);
    }

    private GenericMessage createGenericMessage(TempMessage tempMessage, JsonObject json, String method) {
        JsonObject content = json.getJsonObject("content");
        String message = content.encode();
        Class requestClass = getRequestClass(method);
        Long timestamp = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return new GenericMessage<>(tempMessage.getUser(), Json.decodeValue(message, requestClass), timestamp);
    }
}
