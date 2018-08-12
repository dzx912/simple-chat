package org.training.chat.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.Message;
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
    }

    private void chatAcknowledge(Message<ResponseCreateChat> data) {
        ResponseCreateChat responseCreateChat = data.body();
        UserDto user = responseCreateChat.getAuthor();
        vertx.eventBus().send(DB_FIND_TOKEN_BY_USER.getPath(), user, (AsyncResult<Message<String>> res) -> {
            if (res.succeeded()) {
                String token = res.result().body();
                String pathDevice = String.format(TOKEN.getPath(), token);
                String pathChat = String.format(CHAT.getPath(), responseCreateChat.getChat().getId());

                logger.info("Receiver pathDevice: " + pathDevice);

                String messageToClient = Answerer.createResponseAcknowledge("ack", "createChat", responseCreateChat);
                vertx.eventBus().send(pathDevice, messageToClient);

                // Делаем обработчик события, что кто-то хочет написать в этот WebSocket
                logger.info("Registered pathChat: {}", pathChat);
                vertx.eventBus().localConsumer(pathChat, messageToDevice ->
                        vertx.eventBus().send(pathDevice, messageToDevice.body())
                );
            }
        });
    }

    private void chatCreate(Message<GenericMessage<RequestCreateChat>> data) {
        GenericMessage<RequestCreateChat> request = data.body();
        logger.info("Get message: {}", request);
        RequestCreateChat createdChat = request.getMessage();
        vertx.eventBus().send(DB_CHAT_FIND_BY_LOGIN.getPath(), request, (AsyncResult<Message<Chat>> res) -> {
            if (res.succeeded()) {
                Chat chat = res.result().body();
                ResponseCreateChat responseCreateChat = new ResponseCreateChat(request.getAuthor(), chat);
                logger.info("Find chat by login: {}", responseCreateChat);
                vertx.eventBus().send(CHAT_ACKNOWLEDGE.getPath(), responseCreateChat);
            } else {
                logger.info("Try create chat: {}", request);
                vertx.eventBus().send(DB_CHAT_CREATE_BY_LOGIN.getPath(), request,
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
            vertx.eventBus().send(CHAT_ACKNOWLEDGE.getPath(), responseCreateChat);
        } else {
            logger.warn("Chat is not created: {}", res.cause().getMessage());
        }
    }
}
