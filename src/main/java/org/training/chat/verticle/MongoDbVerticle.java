package org.training.chat.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.training.chat.data.Chat;
import org.training.chat.data.RequestAuthorization;
import org.training.chat.data.TextMessage;
import org.training.chat.data.UserDto;
import org.training.chat.data.db.User;

import java.util.List;
import java.util.Optional;

import static org.training.chat.constants.BusEndpoints.*;

/**
 * Actor, для работы с БД
 */
public class MongoDbVerticle extends AbstractVerticle {
    private final static String DB_NAME = "my_DB";
    private final static String TAG_MESSAGE = "message";
    private final static String TAG_USER = "user";

    private final Logger logger = LogManager.getLogger(MongoDbVerticle.class);

    private MongoClient client;

    @Override
    public void start() {
        logger.debug("Deploy " + MongoDbVerticle.class);
        client = MongoClient.createShared(vertx, new JsonObject()
                .put("db_name", DB_NAME));
        vertx.eventBus().consumer(DB_LOAD_MESSAGES_BY_CHAT.getPath(), this::loadMessageByChat);
        vertx.eventBus().consumer(DB_SAVE_MESSAGE.getPath(), this::saveMessage);
        vertx.eventBus().consumer(DB_REGISTER_USER.getPath(), this::registerUser);
        vertx.eventBus().consumer(DB_FIND_USER.getPath(), this::findUser);
    }

    private void findUser(Message<String> data) {
        String token = data.body();
        JsonObject jsonToken = new JsonObject().put("token", token);
        client.findOne(TAG_USER, jsonToken, null, result -> {
            if (result.succeeded()) {
                Optional<UserDto> userOpt = jsonToUserDto(result.result());
                if (userOpt.isPresent()) {
                    data.reply(userOpt.get());
                } else {
                    data.fail(-2, "User not found");
                }
            } else {
                data.fail(-1, result.cause().getMessage());
            }
        });
    }

    private Optional<UserDto> jsonToUserDto(JsonObject result) {
        logger.info(result);
        if (result != null) {
            UserDto user = new UserDto();
            user.setId(result.getString("_id"));
            user.setLogin(result.getString("login"));
            user.setFirstName(result.getString("firstName"));
            user.setLastName(result.getString("lastName"));
            return Optional.of(user);
        } else {
            return Optional.empty();
        }
    }

    private void loadMessageByChat(Message<Chat> data) {
        Chat chat = data.body();
        JsonObject jsonChat = new JsonObject().put("chatId", chat.getId());
        client.find(TAG_MESSAGE, jsonChat,
                result -> {
                    List<JsonObject> history = result.result();
                    data.reply(Json.encode(history));
                }
        );
    }

    private void saveMessage(Message<TextMessage> data) {
        save(data, data.body(), TAG_MESSAGE);
    }


    private void registerUser(Message<RequestAuthorization> data) {
        User user = new User(data.body());

        JsonObject jsonUser = new JsonObject(Json.encode(user));
        JsonObject query = new JsonObject().put("login", user.getLogin());

        client.findOneAndReplace(TAG_USER, query, jsonUser, replaceResult -> {
            boolean isSucceeded = replaceResult.succeeded() && replaceResult.result() != null;
            if (isSucceeded) {
                logger.debug("Replace: " + replaceResult.result());
                data.reply(user);
            } else {
                createNewUser(data, user, jsonUser);
            }
        });
    }

    private void createNewUser(Message<RequestAuthorization> data, User user, JsonObject jsonUser) {
        client.insert(TAG_USER, jsonUser, savedResult -> {
            if (savedResult.succeeded()) {
                logger.debug("Save: " + savedResult.result());
                data.reply(user);
            } else {
                logger.error("Save: " + savedResult.cause());
                data.fail(-1, savedResult.cause().getMessage());
            }
        });
    }

    private <T, U> void save(Message<T> data, U message, String tag) {
        JsonObject jsonMessage = new JsonObject(Json.encode(message));

        Handler<AsyncResult<String>> resultHandler =
                (savedResult) -> handlerSaved(savedResult, data);

        client.insert(tag, jsonMessage, resultHandler);
    }

    private <T> void handlerSaved(AsyncResult<String> savedResult, Message<T> data) {
        if (savedResult.succeeded()) {
            logger.debug("Save: " + savedResult.result());
            data.reply(savedResult.result());
        } else {
            logger.error("Save: " + savedResult.cause());
            data.fail(-1, savedResult.cause().getMessage());
        }
    }
}
