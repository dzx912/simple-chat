package org.training.chat.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.ServerWebSocket;
import org.training.chat.constants.ServerOption;
import org.training.chat.handler.SendMessageHandler;

import static org.training.chat.constants.BusEndpoints.ROUTER;

/**
 * Actor для приема сообщений
 */
public class WsServerVerticle extends AbstractVerticle {

    private EventBus eventBus;

    @Override
    public void start() {
        eventBus = vertx.eventBus();

        HttpServer httpServer = vertx.createHttpServer();
        httpServer.websocketHandler(this::createWebSocketServer);
        httpServer.listen(ServerOption.getPort());
    }

    private void createWebSocketServer(ServerWebSocket wsServer) {

        // Извлекаем из WebSocket подключение адрес, обычно мы здесь посылаем токен
        String path = wsServer.path();
        System.out.println("Create WebSocket server with path: " + path);

        // Подключаем обработчик WebSocket сообщений
        wsServer.frameHandler(ws -> eventBus.send(ROUTER.getPath(), ws.textData()));

        // Делаем обработчик события, что кто-то хочет написать в этот WebSocket
        MessageConsumer<String> consumerSendMessage = eventBus.localConsumer(path, new SendMessageHandler(wsServer));

        // Снимаем обработчик, после закрытия WebSocket'а
        wsServer.closeHandler(aVoid -> {
            consumerSendMessage.unregister();
            System.out.println("Close connect with: " + path);
        });
    }
}
