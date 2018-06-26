package org.training.chat.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.training.chat.data.UserDto;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.training.chat.constants.BusEndpoints.DB_FIND_USER;
import static org.training.chat.constants.BusEndpoints.VALIDATE_TOKEN;

/**
 * Actor для проверки входящих соединений
 */
public class ValidateTokenVerticle extends AbstractVerticle {
    private final static Pattern PATTERN_TOKEN = Pattern.compile("/token/(.*)");

    private final Logger logger = LogManager.getLogger(ValidateTokenVerticle.class);

    @Override
    public void start() {
        logger.debug("Deploy " + ValidateTokenVerticle.class);

        vertx.eventBus().localConsumer(VALIDATE_TOKEN.getPath(), this::validateToken);
    }

    private void validateToken(Message<String> data) {
        String url = data.body();

        Matcher m = PATTERN_TOKEN.matcher(url);
        if (m.matches()) {
            String token = m.group(1);
            vertx.eventBus().send(DB_FIND_USER.getPath(), token, (AsyncResult<Message<UserDto>> resultUser) -> {
                if (resultUser.succeeded()) {
                    UserDto user = resultUser.result().body();
                    logger.info("User: " + user);
                    data.reply(user);
                } else {
                    data.fail(-2, resultUser.cause().getMessage());
                }
            });
        } else {
            data.fail(-1, "Wrong connect token, correct format: /token/ADDRESS" +
                    ", you write: " + url);
        }
    }
}
