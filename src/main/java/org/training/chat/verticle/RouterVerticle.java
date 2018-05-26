package org.training.chat.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.training.chat.constants.BusEndpoints.ROUTER;
import static org.training.chat.constants.BusEndpoints.TOKEN;

/**
 * Actor для маршрутизации обработки сообщений от клиента
 */
public class RouterVerticle extends AbstractVerticle {

    private final static String WEB_SOCKET_CLOSE = "\u0003�";
    private final Logger logger = LogManager.getLogger(RouterVerticle.class);

    @Override
    public void start() {
        vertx.eventBus().localConsumer(ROUTER.getPath(), this::router);
    }

    private void router(Message<String> data) {
        try {
            String jsonText = data.body();

            if (jsonText.isEmpty() || WEB_SOCKET_CLOSE.equals(jsonText)) {
                data.fail(-2, "Empty json");
                return;
            }
            logger.info("RouterVerticle WebSocket message: " + jsonText);

            final org.training.chat.data.Message message = Json.decodeValue(jsonText, org.training.chat.data.Message.class);

            String token = String.format(TOKEN.getPath(), message.getChat().getId());
            logger.info("Receiver token: " + token);

            vertx.eventBus().send(token, jsonText);

            data.reply("ok");
        } catch (DecodeException exception) {
            logger.info("Wrong data for router: " + exception);
            data.fail(-1, exception.getMessage());
        }
    }
}
