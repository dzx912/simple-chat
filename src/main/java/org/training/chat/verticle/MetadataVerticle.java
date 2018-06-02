package org.training.chat.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.training.chat.data.RequestMessage;
import org.training.chat.data.TempMessage;
import org.training.chat.data.TextMessage;
import org.training.chat.data.User;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.training.chat.constants.BusEndpoints.GENERATE_COMMON_MESSAGE;
import static org.training.chat.constants.BusEndpoints.ROUTER;

/**
 * Actor, который берет TempMessage и генерирует TextMessage
 * TempMessage нужен, чтобы передать данные от клиента и его метаданные в "сыром виде".
 * Например, JSON сообщения в виде текстовой строки.
 * <p>
 * TextMessage нужен, чтобы хранить всю информацию о сообщении в структурированном классе.
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
            TextMessage textMessage = generate(tempMessage, clientMessage);
            vertx.eventBus().send(ROUTER.getPath(), textMessage);
            data.reply("ok");
        } else {
            data.fail(-1, "Empty client message");
        }
    }

    private TextMessage generate(TempMessage tempMessage, String clientMessage) {
        User author = new User(Long.valueOf(tempMessage.getToken()));
        long timestamp = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        RequestMessage requestMessage = Json.decodeValue(clientMessage, RequestMessage.class);
        return new TextMessage(author,
                requestMessage.getChat().getId(),
                requestMessage.getText(),
                requestMessage.getClientId(),
                timestamp);
    }
}
