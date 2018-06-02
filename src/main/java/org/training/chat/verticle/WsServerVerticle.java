package org.training.chat.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.training.chat.constants.ServerOption;
import org.training.chat.data.Chat;
import org.training.chat.data.TextMessage;
import org.training.chat.handler.ReceiveMessageHandler;
import org.training.chat.handler.SendMessageHandler;

import static org.training.chat.constants.BusEndpoints.DB_LOAD_MESSAGES_BY_CHAT;
import static org.training.chat.constants.BusEndpoints.VALIDATE_TOKEN;

/**
 * Actor для приема сообщений
 */
public class WsServerVerticle extends AbstractVerticle {

    private final Logger logger = LogManager.getLogger(WsServerVerticle.class);

    private EventBus eventBus;

    @Override
    public void start() {
        eventBus = vertx.eventBus();

        HttpServer httpServer = vertx.createHttpServer();
        httpServer.websocketHandler(this::createWebSocketServer);
        httpServer.listen(ServerOption.getPort());
        logger.debug("Deploy " + WsServerVerticle.class);
    }

    private void createWebSocketServer(ServerWebSocket wsServer) {

        // Извлекаем из WebSocket подключение адрес, обычно мы здесь посылаем токен
        String path = wsServer.path();
        logger.info("Create WebSocket server with path: " + path);

        validateConnection(wsServer, path);

        sendHistory(wsServer, path);

        // Подключаем обработчик WebSocket сообщений
        wsServer.frameHandler(new ReceiveMessageHandler(vertx, wsServer));

        // Делаем обработчик события, что кто-то хочет написать в этот WebSocket
        MessageConsumer<TextMessage> consumerSendMessage =
                eventBus.localConsumer(path, new SendMessageHandler(wsServer));

        // Снимаем обработчик, после закрытия WebSocket'а
        wsServer.closeHandler(aVoid -> {
            consumerSendMessage.unregister();
            logger.info("Close connect with: " + path);
        });
    }

    private void sendHistory(ServerWebSocket wsServer, String path) {
        String token = path.substring(7);

        Chat chat = new Chat(Long.valueOf(token));
        vertx.eventBus().send(
                DB_LOAD_MESSAGES_BY_CHAT.getPath(),
                chat,
                (AsyncResult<Message<String>> result) -> answerSendHistory(wsServer, result)
        );
    }

    private void answerSendHistory(ServerWebSocket wsServer, AsyncResult<Message<String>> result) {
        String messages = result.result().body();
        JsonArray jsonMessages = new JsonArray(messages);
        JsonObject history = new JsonObject().put("history", jsonMessages);
        wsServer.writeFinalTextFrame(history.encode());
    }

    private void validateConnection(ServerWebSocket wsServer, String path) {
        wsServer.pause();
        vertx.eventBus().send(
                VALIDATE_TOKEN.getPath(),
                path,
                answer -> validateToken(wsServer, answer)
        );
    }

    private void validateToken(ServerWebSocket wsServer, AsyncResult<Message<Object>> answer) {
        if (answer.succeeded()) {
            wsServer.resume();
            logger.debug("Token correct: " + answer.result().body());
        } else {
            String errorMessage = answer.cause().getMessage();
            logger.warn(errorMessage);
            wsServer.close((short) -1, errorMessage);
        }
    }


}
