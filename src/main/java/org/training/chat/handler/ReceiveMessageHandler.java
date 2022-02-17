package org.training.chat.handler;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.WebSocketFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.training.chat.data.TempMessage;
import org.training.chat.data.User;

import static org.training.chat.constants.BusEndpoints.ROUTER_METHOD;

public class ReceiveMessageHandler implements Handler<WebSocketFrame> {

    private final Logger logger = LogManager.getLogger(ReceiveMessageHandler.class);

    private final Vertx vertx;
    private final User user;


    public ReceiveMessageHandler(Vertx vertx, User user) {
        this.user = user;
        this.vertx = vertx;
    }

    @Override
    public void handle(WebSocketFrame webSocketFrame) {
        String message = webSocketFrame.textData();
        logger.info("Message from user: " + message);
        TempMessage tempMessage = new TempMessage(user, message);

        vertx.eventBus().request(ROUTER_METHOD.getPath(), tempMessage);
    }
}
