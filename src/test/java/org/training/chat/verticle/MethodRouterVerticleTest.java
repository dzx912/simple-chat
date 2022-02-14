package org.training.chat.verticle;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.training.chat.codec.Codec;
import org.training.chat.data.GenericMessage;
import org.training.chat.data.RequestTextMessage;
import org.training.chat.data.TempMessage;
import org.training.chat.data.UserDto;

import static org.training.chat.constants.BusEndpoints.ROUTER_CHAT;
import static org.training.chat.constants.BusEndpoints.ROUTER_METHOD;

/**
 * Unit test for actor Method router
 */
@RunWith(VertxUnitRunner.class)
public class MethodRouterVerticleTest {
    private Vertx vertx;

    private static final UserDto USER = new UserDto("id", "login", "firstName", "lastName");

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();

        vertx.eventBus().registerDefaultCodec(TempMessage.class, new Codec<>(TempMessage.class));
        vertx.eventBus().registerDefaultCodec(GenericMessage.class, new Codec<>(GenericMessage.class));

        vertx.deployVerticle(MethodRouterVerticle.class.getName(), context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test(timeout = 10_000)
    public void sendCorrectDataShouldLaunchRouter(TestContext context) {
        final Async async = context.async();

        // Подготовка данных
        String text = "hello";
        Long clientId = 1L;
        String chatId = "2";
        String message = String.format("{\"method\": \"sendTextMessage\",\"content\":{\"clientId\":%d,\"text\":\"%s\",\"chatId\":\"%s\"}}", clientId, text, chatId);
        TempMessage tempMessage = new TempMessage(USER, message);

        // Проверка эксперимента
        vertx.eventBus().localConsumer(ROUTER_CHAT.getPath(), (Message<GenericMessage<RequestTextMessage>> tempMessageData) -> {
            try {
                GenericMessage<RequestTextMessage> genericMessage = tempMessageData.body();

                RequestTextMessage requestTextMessage = genericMessage.getMessage();
                context.assertEquals(text, requestTextMessage.getText());
                context.assertEquals(chatId, requestTextMessage.getChatId());
                context.assertEquals(clientId, requestTextMessage.getClientId());

                context.assertEquals(USER, genericMessage.getAuthor());
                async.complete();
            } catch (ClassCastException exception) {
                context.fail("Wrong JSON TextMessage: " + tempMessageData.body());
            }
        });

        // Запуск проверяемого метода
        vertx.eventBus().send(ROUTER_METHOD.getPath(), tempMessage);
    }

    @Test(timeout = 10_000)
    public void sendSomeTrashShouldReturnFail(TestContext context) {
        final Async async = context.async();


        String badData = "12345";
        TempMessage tempMessage = new TempMessage(USER, badData);
        String errorMessage = String.format("Cannot deserialize data: " +
                        "{\"user\":%s,\"message\":\"%s\"}",
                USER, badData);

        vertx.eventBus().request(ROUTER_METHOD.getPath(), tempMessage, answer -> {
            context.assertTrue(answer.failed());
            context.assertEquals(errorMessage, answer.cause().getMessage());
            async.complete();
        });
    }
}