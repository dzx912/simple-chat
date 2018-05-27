package org.training.chat.handler;

import io.vertx.core.http.ServerWebSocket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handler, который принимает сообщение от клиента -> серверу
 */
public class ReceiveMessageHandler {

    private final Logger logger = LogManager.getLogger(ReceiveMessageHandler.class);

    public ReceiveMessageHandler(ServerWebSocket wsServer) {
        // Извлекаем из WebSocket подключение адрес, обычно мы здесь посылаем токен
        String path = wsServer.path();
    }
}
