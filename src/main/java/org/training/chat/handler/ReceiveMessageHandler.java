package org.training.chat.handler;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocketFrame;
import io.vertx.core.json.Json;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.training.chat.data.TempMessage;
import org.training.chat.data.UserDto;

import static org.training.chat.constants.BusEndpoints.GENERATE_COMMON_MESSAGE;

public class ReceiveMessageHandler implements Handler<WebSocketFrame> {

    private final Logger logger = LogManager.getLogger(ReceiveMessageHandler.class);

    private final Vertx vertx;
    private final UserDto user;


    public ReceiveMessageHandler(Vertx vertx, ServerWebSocket wsServer, UserDto user) {
        this.user = user;
        this.vertx = vertx;
    }

    @Override
    public void handle(WebSocketFrame webSocketFrame) {
        String message = webSocketFrame.textData();
        TempMessage tempMessage = new TempMessage(user, message);
        String json = Json.encode(tempMessage);

        vertx.eventBus().send(GENERATE_COMMON_MESSAGE.getPath(), json);
    }
}
