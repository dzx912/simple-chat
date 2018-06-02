package org.training.chat.handler;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.Json;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.training.chat.data.TextMessage;

/**
 * Handler, который отправляет в WebSocket сообщение от сервера -> клиенту
 */
public class SendMessageHandler implements Handler<Message<TextMessage>> {

    private final Logger logger = LogManager.getLogger(SendMessageHandler.class);

    private ServerWebSocket webSocket;

    public SendMessageHandler(ServerWebSocket webSocket) {
        this.webSocket = webSocket;
    }

    @Override
    public void handle(Message<TextMessage> data) {
        TextMessage message = data.body();
        logger.info("WebSocket message: " + message);
        webSocket.writeFinalTextFrame(Json.encode(message));
        data.reply("ok");
    }
}
