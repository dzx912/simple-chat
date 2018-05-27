package org.training.chat.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.training.chat.data.CommonMessage;
import org.training.chat.data.TextMessage;

import static org.training.chat.constants.BusEndpoints.ROUTER;
import static org.training.chat.constants.BusEndpoints.TOKEN;

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

    private void router(Message<CommonMessage> data) {
        try {
            CommonMessage commonMessage = data.body();

            logger.info("WebSocket commonMessage: " + commonMessage);

            final TextMessage textMessage = commonMessage.getMessage();

            String token = String.format(TOKEN.getPath(), textMessage.getChat().getId());
            logger.info("Receiver token: " + token);

            vertx.eventBus().send(token, commonMessage);

            data.reply("ok");
        } catch (ClassCastException exception) {
            String errorMessage = "Wrong data for router: " + data.body();
            logger.warn(errorMessage);
            data.fail(-1, errorMessage);
        }
    }
}
