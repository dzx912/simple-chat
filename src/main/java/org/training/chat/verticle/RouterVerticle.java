package org.training.chat.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.training.chat.data.GenericMessage;
import org.training.chat.data.RequestTextMessage;
import org.training.chat.data.TextMessage;
import org.training.chat.util.Answerer;

import static org.training.chat.constants.BusEndpoints.*;

/**
 * Actor для маршрутизации обработки сообщений от клиента
 */
public class RouterVerticle extends AbstractVerticle {

    private final Logger logger = LogManager.getLogger(RouterVerticle.class);

    @Override
    public void start() {
        vertx.eventBus().localConsumer(ROUTER_CHAT.getPath(), this::router);
        logger.debug("Deploy " + RouterVerticle.class);
    }

    private void router(Message<GenericMessage<RequestTextMessage>> data) {
        try {
            GenericMessage<RequestTextMessage> genericMessage = data.body();
            TextMessage textMessage = convertGenericMessageToTextMessage(genericMessage);

            logger.info("WebSocket textMessage: " + textMessage);

            String chatPath = String.format(CHAT.getPath(), textMessage.getChatId());
            logger.info("Send to chat: " + chatPath);

            String messageToClient = Answerer.createResponseMessage("text", textMessage);

            vertx.eventBus().send(chatPath, messageToClient);
            vertx.eventBus().send(DB_SAVE_MESSAGE.getPath(), textMessage);

            data.reply("ok");
        } catch (ClassCastException exception) {
            String errorMessage = "Wrong data for router: " + data.body();
            logger.warn(errorMessage);
            data.fail(-1, errorMessage);
        }
    }

    private TextMessage convertGenericMessageToTextMessage(GenericMessage<RequestTextMessage> tempMessage) {
        RequestTextMessage requestTextMessage = tempMessage.getMessage();
        return new TextMessage(tempMessage.getAuthor(),
                requestTextMessage.getChatId(),
                requestTextMessage.getText(),
                requestTextMessage.getClientId(),
                tempMessage.getTimestamp());
    }
}
