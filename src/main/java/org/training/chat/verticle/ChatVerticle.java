package org.training.chat.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.training.chat.data.*;
import org.training.chat.util.Answerer;

import static org.training.chat.constants.BusEndpoints.*;

/**
 * Actor, который управляет чатами.
 * Создает их и выдает ID чата по атрибутам
 */
public class ChatVerticle extends AbstractVerticle {
    private final Logger logger = LogManager.getLogger(ChatVerticle.class);

    @Override
    public void start() {
        vertx.eventBus().localConsumer(CHAT_CREATE.getPath(), this::chatCreate);
        vertx.eventBus().localConsumer(CHAT_ACKNOWLEDGE.getPath(), this::chatAcknowledge);
        vertx.eventBus().localConsumer(CHAT_GET_HISTORY.getPath(), this::chatGetHistory);
    }

    private void chatCreate(Message<GenericMessage<RequestCreateChat>> data) {
        GenericMessage<RequestCreateChat> request = data.body();
        logger.info("Get message: {}", request);
        vertx.eventBus().request(DB_CHAT_FIND_BY_LOGIN.getPath(), request, (AsyncResult<Message<Chat>> res) -> {
            if (res.succeeded()) {
                Chat chat = res.result().body();
                ResponseCreateChat responseCreateChat = new ResponseCreateChat(request.getAuthor(), chat);
                logger.info("Find chat by login: {}", responseCreateChat);
                vertx.eventBus().request(CHAT_ACKNOWLEDGE.getPath(), responseCreateChat);
            } else {
                logger.info("Try create chat: {}", request);
                vertx.eventBus().request(DB_CHAT_CREATE_BY_LOGIN.getPath(), request,
                        (AsyncResult<Message<Chat>> resCreating) -> acknowledge(resCreating, request.getAuthor()));
            }
        });
        data.reply("ok");
    }

    private void acknowledge(AsyncResult<Message<Chat>> res, UserDto author) {
        if (res.succeeded()) {
            Chat chat = res.result().body();
            ResponseCreateChat responseCreateChat = new ResponseCreateChat(author, chat);
            logger.info("Chat is created: {}", responseCreateChat);
            vertx.eventBus().request(CHAT_ACKNOWLEDGE.getPath(), responseCreateChat);
        } else {
            logger.warn("Chat is not created: {}", res.cause().getMessage());
        }
    }

    private void chatAcknowledge(Message<ResponseCreateChat> data) {
        ResponseCreateChat responseCreateChat = data.body();
        UserDto user = responseCreateChat.author();
        vertx.eventBus().request(DB_FIND_TOKEN_BY_USER.getPath(), user, (AsyncResult<Message<String>> res) -> {
            if (res.succeeded()) {
                String token = res.result().body();
                String pathDevice = String.format(TOKEN.getPath(), token);
                String pathChat = String.format(CHAT.getPath(), responseCreateChat.chat().id());

                logger.info("Receiver pathDevice: " + pathDevice);

                String messageToClient = Answerer.createResponseAcknowledge("ack", "createChat", responseCreateChat);
                vertx.eventBus().request(pathDevice, messageToClient);

                // Делаем обработчик события, что кто-то хочет написать в этот WebSocket
                logger.info("Registered pathChat: {}", pathChat);
                vertx.eventBus().localConsumer(pathChat, messageToDevice ->
                        vertx.eventBus().request(pathDevice, messageToDevice.body())
                );

                vertx.eventBus().request(CHAT_GET_HISTORY.getPath(), responseCreateChat.chat(),
                        (AsyncResult<Message<String>> result) -> answerSendHistory(pathDevice, result)
                );
            }
        });
    }

    private void answerSendHistory(String pathDevice, AsyncResult<Message<String>> result) {
        String messages = result.result().body();
        String responseHistory = createResponseHistory(messages);
        vertx.eventBus().request(pathDevice, responseHistory);
    }

    private String createResponseHistory(String messages) {
        JsonArray jsonMessages = new JsonArray(messages);
        JsonObject history = new JsonObject().put("history", jsonMessages);
        ResponseMessage response = new ResponseMessage("history", history);
        return Json.encode(response);
    }

    private void chatGetHistory(Message<Chat> data) {
        Chat chat = data.body();
        vertx.eventBus().request(
                DB_LOAD_MESSAGES_BY_CHAT.getPath(),
                chat,
                answer -> data.reply(answer.result().body())
        );
    }
}
