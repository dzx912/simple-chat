package org.training.chat.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.training.chat.data.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.training.chat.constants.BusEndpoints.GENERATE_COMMON_MESSAGE;
import static org.training.chat.constants.BusEndpoints.ROUTER;

/**
 * Actor, который берет TempMessage и генерирует CommonMessage
 * TempMessage нужен, чтобы передать данные от клиента и его метаданные в "сыром виде".
 * Например, JSON сообщения в виде текстовой строки.
 * <p>
 * CommonMessage нужен, чтобы хранить всю информацию о сообщении в структурированном классе.
 */
public class MetadataVerticle extends AbstractVerticle {
    private final static String WEB_SOCKET_CLOSE = "\u0003�";

    private final Logger logger = LogManager.getLogger(MetadataVerticle.class);

    @Override
    public void start() {
        logger.debug("Deploy " + MetadataVerticle.class);
        vertx.eventBus().localConsumer(GENERATE_COMMON_MESSAGE.getPath(), this::generateCommonMessage);
    }

    private void generateCommonMessage(Message<String> data) {
        String jsonRequest = data.body();
        TempMessage tempMessage = Json.decodeValue(jsonRequest, TempMessage.class);
        String clientMessage = tempMessage.getMessage();

        boolean webSocketIsClosed = clientMessage.isEmpty() || WEB_SOCKET_CLOSE.equals(clientMessage);
        if (!webSocketIsClosed) {
            CommonMessage commonMessage = generate(tempMessage, clientMessage);
            vertx.eventBus().send(ROUTER.getPath(), commonMessage);
            data.reply("ok");
        } else {
            data.fail(-1, "Empty client message");
        }
    }

    private CommonMessage generate(TempMessage tempMessage, String clientMessage) {
        User author = new User(Long.valueOf(tempMessage.getToken()));
        long timestamp = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        Metadata metadata = new Metadata(author, timestamp);

        TextMessage textMessage = Json.decodeValue(clientMessage, TextMessage.class);
        return new CommonMessage(metadata, textMessage);
    }
}
