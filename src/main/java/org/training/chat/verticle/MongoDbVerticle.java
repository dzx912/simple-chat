package org.training.chat.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.training.chat.data.*;
import org.training.chat.data.db.User;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.training.chat.constants.BusEndpoints.*;

/**
 * Actor, для работы с БД
 */
public class MongoDbVerticle extends AbstractVerticle {
    private final static String DB_NAME = "messenger";
    private final static String TAG_MESSAGE = "message";
    private final static String TAG_USER = "user";
    private final static String TAG_CHAT = "chat";

    private final Logger logger = LogManager.getLogger(MongoDbVerticle.class);

    private MongoClient client;

    @Override
    public void start() {
        client = MongoClient.createShared(vertx, new JsonObject()
                .put("db_name", DB_NAME));
        vertx.eventBus().consumer(DB_LOAD_MESSAGES_BY_CHAT.getPath(), this::loadMessageByChat);
        vertx.eventBus().consumer(DB_SAVE_MESSAGE.getPath(), this::saveMessage);
        vertx.eventBus().consumer(DB_REGISTER_USER.getPath(), this::registerUser);
        vertx.eventBus().consumer(DB_FIND_USER.getPath(), this::findUser);
        vertx.eventBus().consumer(DB_CHAT_CREATE_BY_LOGIN.getPath(), this::createChat);
        vertx.eventBus().consumer(DB_CHAT_FIND_BY_LOGIN.getPath(), this::findChatByLogin);
        vertx.eventBus().consumer(DB_FIND_TOKEN_BY_USER.getPath(), this::findTokenByUser);

        logger.debug("Deploy " + MongoDbVerticle.class);
    }

    private void findTokenByUser(Message<UserDto> data) {
        UserDto sender = data.body();

        JsonObject jsonReceiverLogin = new JsonObject().put("login", sender.login());
        client.findOne(TAG_USER, jsonReceiverLogin, null, result -> {
            if (result.succeeded()) {
                String token = result.result().getString("token");
                if (token != null && !token.isEmpty()) {
                    data.reply(token);
                } else {
                    data.fail(-2, "Token not found");
                }
            } else {
                data.fail(-1, result.cause().getMessage());
            }
        });
    }

    private void findChatByLogin(Message<GenericMessage<RequestCreateChat>> data) {
        GenericMessage<RequestCreateChat> requestCreateChat = data.body();
        RequestCreateChat createChat = requestCreateChat.getMessage();
        String senderLogin = requestCreateChat.getAuthor().login();
        String receiverLogin = createChat.getLoginReceiver();

        // Ищим чат, в котом ровно 2 пользователя
        // и их логины совпадают с логинами создателя и его приятеля
        JsonObject jsonQuery = new JsonObject()
                .put("users.login", senderLogin)
                .put("users.login", receiverLogin)
                .put("users", new JsonObject().put("$size", 2));

        client.findOne(TAG_CHAT, jsonQuery, null, result -> {
            if (result.succeeded()) {
                Optional<Chat> chatOpt = jsonToChat(result.result());
                if (chatOpt.isPresent()) {
                    Chat receiver = chatOpt.get();
                    data.reply(receiver);
                } else {
                    data.fail(-2, "Chat not found");
                }
            } else {
                data.fail(-1, result.cause().getMessage());
            }
        });
    }

    private void createChat(Message<GenericMessage<RequestCreateChat>> data) {
        GenericMessage<RequestCreateChat> request = data.body();
        UserDto sender = request.getAuthor();
        String receiverLogin = request.getMessage().getLoginReceiver();

        JsonObject jsonReceiverLogin = new JsonObject().put("login", receiverLogin);
        client.findOne(TAG_USER, jsonReceiverLogin, null, result -> {
            if (result.succeeded()) {
                Optional<UserDto> userOpt = jsonToUserDto(result.result());
                if (userOpt.isPresent()) {
                    UserDto receiver = userOpt.get();
                    createChatByIds(data, sender, receiver);
                } else {
                    data.fail(-2, "User not found");
                }
            } else {
                data.fail(-1, result.cause().getMessage());
            }
        });
    }

    private <T> void createChatByIds(Message<T> data, UserDto user1, UserDto user2) {
        List<UserDto> users = Arrays.asList(user1, user2);
        String jsonUsers = Json.encode(users);
        JsonObject requestJsonUsers = new JsonObject()
                .put("users", new JsonArray(jsonUsers));

        client.insert(TAG_CHAT, requestJsonUsers, savedResult -> {
            if (savedResult.succeeded()) {
                logger.debug("Save: " + savedResult.result());
                Chat chat = new Chat(savedResult.result(), users);
                data.reply(chat);
            } else {
                logger.error("Save: " + savedResult.cause());
                data.fail(-1, savedResult.cause().getMessage());
            }
        });
    }

    private void findUser(Message<String> data) {
        String token = data.body();
        JsonObject jsonToken = new JsonObject().put("token", token);
        client.findOne(TAG_USER, jsonToken, null,
                result -> answerAboutFindUser(data, result)
        );
    }

    private void answerAboutFindUser(Message<String> data, AsyncResult<JsonObject> result) {
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
    }

    private Optional<UserDto> jsonToUserDto(JsonObject result) {
        logger.info(result);
        if (result != null) {
            UserDto user = new UserDto(
                    result.getString("_id"),
                    result.getString("login"),
                    result.getString("firstName"),
                    result.getString("lastName"));
            return Optional.of(user);
        } else {
            return Optional.empty();
        }
    }

    private Optional<Chat> jsonToChat(JsonObject result) {
        logger.info(result);
        if (result != null) {
            Chat chat = new Chat();
            chat.setId(result.getString("_id"));
            return Optional.of(chat);
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
}
