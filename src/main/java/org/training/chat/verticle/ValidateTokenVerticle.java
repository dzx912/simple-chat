package org.training.chat.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.training.chat.constants.BusEndpoints.VALIDATE_TOKEN;

/**
 * Actor для проверки входящих соединений
 */
public class ValidateTokenVerticle extends AbstractVerticle {
    private final Logger logger = LogManager.getLogger(ValidateTokenVerticle.class);

    @Override
    public void start() {
        logger.debug("Deploy " + ValidateTokenVerticle.class);

        vertx.eventBus().localConsumer(VALIDATE_TOKEN.getPath(), this::validateToken);
    }

    private void validateToken(Message<String> data) {
        String token = data.body();
        Pattern pattern = Pattern.compile("/token/.*");
        Matcher m = pattern.matcher(token);
        if (m.matches()) {
            data.reply(token);
        } else {
            data.fail(-1, "Wrong connect token, correct format: /token/ADDRESS" +
                    ", you write: " + token);
        }
    }
}
