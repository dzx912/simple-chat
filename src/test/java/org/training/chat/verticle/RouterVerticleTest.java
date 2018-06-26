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
import org.training.chat.data.Chat;
import org.training.chat.data.RequestMessage;
import org.training.chat.data.TextMessage;
import org.training.chat.data.UserDto;

import static org.training.chat.constants.BusEndpoints.ROUTER;
import static org.training.chat.constants.BusEndpoints.TOKEN;

/**
 * Unit test for actor Router
 */
@RunWith(VertxUnitRunner.class)
public class RouterVerticleTest {

    private Vertx vertx;

    private long idChat = 3L;
    private TextMessage correctMessage;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();

        vertx.eventBus().registerDefaultCodec(TextMessage.class, new Codec<>(TextMessage.class));

        vertx.deployVerticle(RouterVerticle.class.getName(), context.asyncAssertSuccess());

        RequestMessage requestMessage =
                new RequestMessage(2L, "text message", new Chat(idChat));
        correctMessage = new TextMessage(
                new UserDto("id", "login", "firstName", "lastName"),
                idChat,
                "text message",
                3L,
                10L
        );
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test(timeout = 10_000)
    public void routerShouldSendCorrectData(TestContext context) {
        final Async async = context.async();

        String tokenUser = String.format(TOKEN.getPath(), idChat);

        vertx.eventBus().localConsumer(tokenUser, receiveResult -> {
            context.assertEquals(correctMessage, receiveResult.body());
            async.complete();
        });

        vertx.eventBus().send(ROUTER.getPath(), correctMessage);
    }

    @Test
    public void sendUncorrectedDataShouldFailed(TestContext context) {
        final Async async = context.async();

        String uncorrectedMessage = "uncorrected json";

        vertx.eventBus().send(ROUTER.getPath(), uncorrectedMessage, answer -> {
            context.assertTrue(answer.failed());
            context.assertEquals("Wrong data for router: " + uncorrectedMessage, answer.cause().getMessage());
            async.complete();
        });
    }

    @Test
    public void sendCorrectDataShouldSuccess(TestContext context) {
        final Async async = context.async();

        vertx.eventBus().send(ROUTER.getPath(), correctMessage, answer -> {
            context.assertTrue(answer.succeeded());
            async.complete();
        });
    }
}
