package org.training.chat.verticle;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.training.chat.codec.Codec;
import org.training.chat.data.TempMessage;
import org.training.chat.data.TextMessage;
import org.training.chat.data.UserDto;

import static org.training.chat.constants.BusEndpoints.GENERATE_COMMON_MESSAGE;
import static org.training.chat.constants.BusEndpoints.ROUTER;

/**
 * Unit test for actor Metadata
 */
@RunWith(VertxUnitRunner.class)
public class MetadataVerticleTest {

    private Vertx vertx;

    private static final UserDto USER = new UserDto("id", "login", "firstName", "lastName");

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();

        vertx.eventBus().registerDefaultCodec(TextMessage.class, new Codec<>(TextMessage.class));

        vertx.deployVerticle(MetadataVerticle.class.getName(), context.asyncAssertSuccess());
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
        Long chatId = 2L;
        String message = String.format("{\"clientId\":%d,\"text\":\"%s\",\"chat\":{\"id\":%d}}", clientId, text, chatId);
        TempMessage tempMessage = new TempMessage(USER, message);
        String json = Json.encode(tempMessage);

        // Проверка эксперимента
        vertx.eventBus().localConsumer(ROUTER.getPath(), (Message<TextMessage> tempMessageData) -> {
            try {
                TextMessage textMessage = tempMessageData.body();

                context.assertEquals(text, textMessage.getText());
                context.assertEquals(chatId, textMessage.getChatId());
                context.assertEquals(clientId, textMessage.getClientId());

                context.assertEquals(USER, textMessage.getAuthor());
                async.complete();
            } catch (ClassCastException exception) {
                context.fail("Wrong JSON TextMessage: " + tempMessageData.body());
            }
        });

        // Запуск проверяемого метода
        vertx.eventBus().send(GENERATE_COMMON_MESSAGE.getPath(), json);
    }

    @Test(timeout = 10_000)
    public void emptyDataFormClientShouldReturnFail(TestContext context) {
        final Async async = context.async();

        TempMessage tempMessage = new TempMessage(USER, "");
        String json = Json.encode(tempMessage);

        vertx.eventBus().send(GENERATE_COMMON_MESSAGE.getPath(), json, answer -> {
            context.assertTrue(answer.failed());
            context.assertEquals("Empty client message", answer.cause().getMessage());
            async.complete();
        });
    }

    @Test(timeout = 10_000)
    public void sendSomeTrashShouldReturnFail(TestContext context) {
        final Async async = context.async();

        String badData = "12345";
        String errorMessage = String.format("Cannot deserialize data: %s", badData);

        vertx.eventBus().send(GENERATE_COMMON_MESSAGE.getPath(), badData, answer -> {
            context.assertTrue(answer.failed());
            context.assertEquals(errorMessage, answer.cause().getMessage());
            async.complete();
        });
    }
}