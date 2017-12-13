package org.training.chat.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocketFrame;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Класс для рассылки сообщений
 */
public class ChatVerticle extends AbstractVerticle {

    private ConcurrentHashMap<String, ServerWebSocket> serverWebSockets;

    @Override
    public void start() {
        serverWebSockets = new ConcurrentHashMap<>();

        HttpServer httpServer = vertx.createHttpServer();
        httpServer.websocketHandler(this::createWebSocketServer);
        httpServer.listen(8080);
    }

    private void createWebSocketServer(ServerWebSocket wsServer) {

        // Извлекаем из WebSocket подключение адрес, обычно мы здесь посылаем токен
        String path = wsServer.path();
        serverWebSockets.put(path, wsServer);
        System.out.println("Create WebSocket server with path: " + path);

        // Подключаем обработчик WebSocket сообщений
        wsServer.frameHandler(this::echoWebSocketReact);
    }

    /**
     * Обработчик приема WebSocket сообщения
     *
     * @param webSocketFrame объект, в котором находится полученное сообщение от клиента
     */
    private void echoWebSocketReact(WebSocketFrame webSocketFrame) {
        String message = webSocketFrame.textData();
        System.out.println("WebSocket message: " + message);

        serverWebSockets.forEach((k, webSocket) -> webSocket.writeFinalTextFrame("Echo message: " + message));
    }
}
