package org.training.chat.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.ServerWebSocket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.training.chat.constants.ServerOption;
import org.training.chat.data.UserDto;
import org.training.chat.handler.ReceiveMessageHandler;
import org.training.chat.handler.SendMessageHandler;

import static org.training.chat.constants.BusEndpoints.VALIDATE_TOKEN;

/**
 * Actor, обслуживающий WebSocket соединение
 */
public class WsServerVerticle extends AbstractVerticle {

    private final Logger logger = LogManager.getLogger(WsServerVerticle.class);

    private EventBus eventBus;

    @Override
    public void start() {
        eventBus = vertx.eventBus();

        HttpServer httpServer = vertx.createHttpServer();
        httpServer.websocketHandler(this::createWebSocketServer);
        httpServer.listen(ServerOption.getWsPort());
        logger.debug("Deploy " + WsServerVerticle.class);


    }

    private void createWebSocketServer(ServerWebSocket wsServer) {
        // Извлекаем из WebSocket подключение адрес, обычно мы здесь посылаем токен
        String path = wsServer.path();
        logger.info("Create WebSocket server with path: " + path);

        validateConnection(wsServer, path);

        // Делаем обработчик события, что кто-то хочет написать в этот WebSocket
        MessageConsumer<String> consumerSendMessage =
                eventBus.localConsumer(path, new SendMessageHandler(wsServer));

        // Снимаем обработчик, после закрытия WebSocket'а
        wsServer.closeHandler(aVoid -> {
            consumerSendMessage.unregister();
            logger.info("Close connect with: " + path);
        });
    }

    private void validateConnection(ServerWebSocket wsServer, String path) {
        wsServer.pause();
        vertx.eventBus().send(
                VALIDATE_TOKEN.getPath(),
                path,
                (AsyncResult<Message<UserDto>> answer) -> validateToken(wsServer, answer)
        );
    }

    private void validateToken(ServerWebSocket wsServer, AsyncResult<Message<UserDto>> answer) {
        if (answer.succeeded()) {
            wsServer.resume();
            UserDto user = answer.result().body();
            logger.debug("Token correct: " + user);

            // Подключаем обработчик WebSocket сообщений
            wsServer.frameHandler(new ReceiveMessageHandler(vertx, user));
        } else {
            String errorMessage = answer.cause().getMessage();
            logger.warn(errorMessage);
            wsServer.close((short) -1, errorMessage);
        }
    }


}
