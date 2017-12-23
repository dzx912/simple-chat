package org.training.chat.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocketFrame;
import org.training.chat.handler.SendMessageHandler;

/**
 * Класс для приема сообщений
 */
public class ReceiveVerticle extends AbstractVerticle {

    private EventBus eventBus;

    @Override
    public void start() {
        eventBus = vertx.eventBus();

        HttpServer httpServer = vertx.createHttpServer();
        httpServer.websocketHandler(this::createWebSocketServer);
        httpServer.listen(8080);
    }

    private void createWebSocketServer(ServerWebSocket wsServer) {

        // Извлекаем из WebSocket подключение адрес, обычно мы здесь посылаем токен
        String path = wsServer.path();
        System.out.println("Create WebSocket server with path: " + path);

        // Подключаем обработчик WebSocket сообщений
        wsServer.frameHandler(this::receiveMessage);

        // Делаем обработчик события, что кто-то хочет написать в этот WebSocket
        MessageConsumer<String> consumerSendMessage = eventBus.consumer(path, new SendMessageHandler(wsServer));

        // Снимаем обработчик, после закрытия WebSocket'а
        wsServer.closeHandler(aVoid -> consumerSendMessage.unregister());
    }

    /**
     * Обработчик приема WebSocket сообщения
     *
     * @param webSocketFrame объект, в котором находится полученное сообщение от клиента
     */
    private void receiveMessage(WebSocketFrame webSocketFrame) {
        String message = webSocketFrame.textData();
        System.out.println("WebSocket message: " + message);
        eventBus.send(message, "hello + " + message);
    }
}
