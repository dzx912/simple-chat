package org.training.chat.verticle;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.training.chat.codec.Codec;
import org.training.chat.data.*;

import java.util.Collections;

import static org.training.chat.constants.BusEndpoints.*;

@RunWith(VertxUnitRunner.class)
public class ChatVerticleTest {
    private final static String TEXT_HISTORY = "{\"type\":\"history\",\"content\":{\"history\":[]}}";
    private final static User USER = new User("1", "dzx912", "Anton", "Lenok");
    private final static GenericMessage<RequestCreateChat> MESSAGE_CREATE_CHAT = new GenericMessage<>(USER, new RequestCreateChat("1"), 10L);
    private final static String CHAT_ID = "test_chat_id";
    private final static String PATH_DEVICE = String.format(TOKEN.getPath(), "test_token");

    private Vertx vertx;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();

        vertx.eventBus().registerDefaultCodec(GenericMessage.class, new Codec<>(GenericMessage.class));
        vertx.eventBus().registerDefaultCodec(RequestCreateChat.class, new Codec<>(RequestCreateChat.class));
        vertx.eventBus().registerDefaultCodec(ResponseCreateChat.class, new Codec<>(ResponseCreateChat.class));
        vertx.eventBus().registerDefaultCodec(Chat.class, new Codec<>(Chat.class));
        vertx.eventBus().registerDefaultCodec(User.class, new Codec<>(User.class));

        vertx.deployVerticle(ChatVerticle.class.getName(), context.asyncAssertSuccess());

        deployCommonConsumer();
    }

    private void deployCommonConsumer() {
        vertx.eventBus().localConsumer(VALIDATE_TOKEN.getPath(),
                data -> data.reply(USER)
        );
        vertx.eventBus().localConsumer(DB_FIND_TOKEN_BY_USER.getPath(),
                data -> data.reply("test_token")
        );
        vertx.eventBus().consumer(DB_CHAT_FIND_BY_LOGIN.getPath(), (data) ->
                data.reply(new Chat(CHAT_ID, Collections.emptyList()))
        );
        vertx.eventBus().localConsumer(DB_LOAD_MESSAGES_BY_CHAT.getPath(),
                data -> data.reply("[]")
        );
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test(timeout = 10_000)
    public void afterCreateOrOpenChatShouldGetHistory(TestContext context) {
        final Async async = context.async();
        vertx.eventBus().localConsumer(PATH_DEVICE, receiveResult -> {
            System.out.println(receiveResult.body());
            if (TEXT_HISTORY.equals(receiveResult.body())) {
                async.complete();
            }
        });
        vertx.eventBus().send(CHAT_CREATE.getPath(), MESSAGE_CREATE_CHAT);
    }
}