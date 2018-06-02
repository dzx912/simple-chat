package org.training.chat.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.training.chat.data.TextMessage;

import static org.training.chat.constants.BusEndpoints.*;

/**
 * Actor для маршрутизации обработки сообщений от клиента
 */
public class RouterVerticle extends AbstractVerticle {

    private final Logger logger = LogManager.getLogger(RouterVerticle.class);

    @Override
    public void start() {
        vertx.eventBus().localConsumer(ROUTER.getPath(), this::router);
        logger.debug("Deploy " + RouterVerticle.class);
    }

    private void router(Message<TextMessage> data) {
        try {
            TextMessage textMessage = data.body();

            logger.info("WebSocket textMessage: " + textMessage);

            String token = String.format(TOKEN.getPath(), textMessage.getChatId());
            logger.info("Receiver token: " + token);

            vertx.eventBus().send(token, textMessage);
            vertx.eventBus().send(DB_SAVE_MESSAGE.getPath(), textMessage);

            data.reply("ok");
        } catch (ClassCastException exception) {
            String errorMessage = "Wrong data for router: " + data.body();
            logger.warn(errorMessage);
            data.fail(-1, errorMessage);
        }
    }
}
