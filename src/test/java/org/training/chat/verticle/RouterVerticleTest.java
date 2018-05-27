package org.training.chat.verticle;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.training.chat.codec.CommonMessageCodec;
import org.training.chat.data.*;

import static org.training.chat.constants.BusEndpoints.ROUTER;
import static org.training.chat.constants.BusEndpoints.TOKEN;

/**
 * Unit test for actor Router
 */
@RunWith(VertxUnitRunner.class)
public class RouterVerticleTest {

    private Vertx vertx;

    private long idChat = 3L;
    private CommonMessage correctMessage;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();

        vertx.eventBus().registerDefaultCodec(CommonMessage.class, new CommonMessageCodec());

        vertx.deployVerticle(RouterVerticle.class.getName(), context.asyncAssertSuccess());

        TextMessage textMessage =
                new TextMessage(2L, "text message", new Chat(idChat));
        correctMessage = new CommonMessage(
                new Metadata(
                        new User(1L),
                        10L),

                textMessage
        );
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
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
    public void sendUncorrectedDataShouldShouldFailed(TestContext context) {
        final Async async = context.async();

        String uncorrectedMessage = "uncorrected json";

        vertx.eventBus().send(ROUTER.getPath(), uncorrectedMessage, answer -> {
            context.assertTrue(answer.failed());
            context.assertEquals("Wrong data for router: " + uncorrectedMessage, answer.cause().getMessage());
            async.complete();
        });
    }

    @Test
    public void sendCorrectDataShouldShouldSuccess(TestContext context) {
        final Async async = context.async();

        vertx.eventBus().send(ROUTER.getPath(), correctMessage, answer -> {
            context.assertTrue(answer.succeeded());
            async.complete();
        });
    }
}
