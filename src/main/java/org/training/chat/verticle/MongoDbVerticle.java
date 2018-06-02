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
import org.training.chat.data.TextMessage;

import java.util.List;

import static org.training.chat.constants.BusEndpoints.DB_LOAD_MESSAGES_BY_CHAT;
import static org.training.chat.constants.BusEndpoints.DB_SAVE_MESSAGE;

/**
 * Actor, для работы с БД
 */
public class MongoDbVerticle extends AbstractVerticle {
    private final static String DB_NAME = "my_DB";
    private final static String DB_TAG = "message";

    private final Logger logger = LogManager.getLogger(MongoDbVerticle.class);

    private MongoClient client;

    @Override
    public void start() {
        logger.debug("Deploy " + MongoDbVerticle.class);
        client = MongoClient.createShared(vertx, new JsonObject()
                .put("db_name", DB_NAME));
        vertx.eventBus().consumer(DB_LOAD_MESSAGES_BY_CHAT.getPath(), this::loadMessageByChat);
        vertx.eventBus().consumer(DB_SAVE_MESSAGE.getPath(), this::saveMessage);
    }

    private void loadMessageByChat(Message<Chat> data) {
        Chat chat = data.body();
        JsonObject jsonChat = new JsonObject().put("chatId", chat.getId());
        client.find(DB_TAG, jsonChat,
                result -> {
                    List<JsonObject> history = result.result();
                    data.reply(Json.encode(history));
                }
        );
    }

    private void saveMessage(Message<TextMessage> data) {
        TextMessage message = data.body();
        JsonObject jsonMessage = new JsonObject(Json.encode(message));

        Handler<AsyncResult<String>> resultHandler =
                (savedResult) -> handlerSaved(savedResult, data);

        client.insert(DB_TAG, jsonMessage, resultHandler);
    }

    private void handlerSaved(AsyncResult<String> savedResult, Message<TextMessage> data) {
        if (savedResult.succeeded()) {
            logger.debug("Save: " + savedResult.result());
        } else {
            logger.error("Save: " + savedResult.cause());
        }
    }
}
