package org.training.chat.integration.test;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.training.chat.codec.Codec;
import org.training.chat.data.*;
import org.training.chat.integration.client.WSClient;
import org.training.chat.verticle.*;

import static org.training.chat.constants.BusEndpoints.DB_FIND_USER;
import static org.training.chat.constants.BusEndpoints.DB_LOAD_MESSAGES_BY_CHAT;
import static org.training.chat.constants.BusEndpoints.DB_SAVE_MESSAGE;

/**
 * Интеграционный тест, проверяющий отправку и доставку сообщений
 * Иногда может не проходить. Для интеграционных тестов это нормально, единственное простое решение - перезапустить.
 */
@RunWith(VertxUnitRunner.class)
public class WebSocketIntegrationTest {
    private final static String WEB_SOCKET_CLOSE = "\u0003�";

    private final Logger logger = LogManager.getLogger(WebSocketIntegrationTest.class);

    private Vertx vertx;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();

        vertx.eventBus().registerDefaultCodec(TempMessage.class, new Codec<>(TempMessage.class));
        vertx.eventBus().registerDefaultCodec(TextMessage.class, new Codec<>(TextMessage.class));
        vertx.eventBus().registerDefaultCodec(Chat.class, new Codec<>(Chat.class));
        vertx.eventBus().registerDefaultCodec(UserDto.class, new Codec<>(UserDto.class));
        vertx.eventBus().registerDefaultCodec(GenericMessage.class, new Codec<>(GenericMessage.class));

        vertx.deployVerticle(WsServerVerticle.class.getName(), context.asyncAssertSuccess());
        vertx.deployVerticle(RouterVerticle.class.getName(), context.asyncAssertSuccess());
        vertx.deployVerticle(ValidateTokenVerticle.class.getName(), context.asyncAssertSuccess());
        vertx.deployVerticle(MethodRouterVerticle.class.getName(), context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test(timeout = 10_000)
    public void client2ShouldReceiveMessageFormClient1(TestContext context) {
        final Async async = context.async();

        vertx.eventBus().consumer(DB_LOAD_MESSAGES_BY_CHAT.getPath(), (empty) -> {
        });
        vertx.eventBus().consumer(DB_SAVE_MESSAGE.getPath(), (empty) -> {
        });
        vertx.eventBus().consumer(DB_FIND_USER.getPath(), (data) ->
            data.reply(new UserDto("1", "dzx912", "Anton", "Lenok"))
        );

        WSClient client1 = new WSClient(vertx, "1");
        WSClient client2 = new WSClient(vertx, "2");

        String text = "{\"method\":\"sendTextMessage\",\"content\":{\"clientId\":1,\"text\":\"hello\",\"chatId\":\"2\"}}";
        String answerStartExpected = "{\"type\":\"text\",\"content\":{\"author\":" +
                "{\"id\":\"1\",\"login\":\"dzx912\",\"firstName\":\"Anton\",\"lastName\":\"Lenok\"}," +
                "\"chatId\":\"2\",\"text\":\"hello\",\"clientId\":1,\"timestamp\":";

        client1.setSendText(text);
        client2.setHandler(receiveText -> {
            logger.info("Receive text: " + receiveText);
            boolean isCloseWebSocket = WEB_SOCKET_CLOSE.equals(receiveText);
            if (!isCloseWebSocket) {
                context.assertEquals(answerStartExpected,
                        receiveText.substring(0, answerStartExpected.length()));

                client2.close();
                client1.close();

                async.complete();
            }
        });

        client1.run();
        client2.run();
    }
}
