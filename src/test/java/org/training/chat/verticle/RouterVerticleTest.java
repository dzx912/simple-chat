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
import org.training.chat.data.GenericMessage;
import org.training.chat.data.RequestTextMessage;
import org.training.chat.data.TextMessage;
import org.training.chat.data.UserDto;

import static org.training.chat.constants.BusEndpoints.ROUTER_CHAT;
import static org.training.chat.constants.BusEndpoints.TOKEN;

/**
 * Unit test for actor Router
 */
@RunWith(VertxUnitRunner.class)
public class RouterVerticleTest {

    private Vertx vertx;

    private String idChat = "3";
    private GenericMessage<RequestTextMessage> correctGenericMessage;
    private TextMessage correctTextMessage;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();

        vertx.eventBus().registerDefaultCodec(GenericMessage.class, new Codec<>(GenericMessage.class));
        vertx.eventBus().registerDefaultCodec(TextMessage.class, new Codec<>(TextMessage.class));

        vertx.deployVerticle(RouterVerticle.class.getName(), context.asyncAssertSuccess());

        UserDto user = new UserDto("id", "login", "firstName", "lastName");
        String text = "text message";
        long clientId = 3L;
        long timestamp = 10L;
        correctGenericMessage = new GenericMessage<>(
                user,
                new RequestTextMessage(
                        clientId,
                        text,
                        idChat),
                timestamp
        );

        correctTextMessage = new TextMessage(user, idChat, text, clientId, timestamp);
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
            context.assertEquals(correctTextMessage, receiveResult.body());
            async.complete();
        });

        vertx.eventBus().send(ROUTER_CHAT.getPath(), correctGenericMessage);
    }

    @Test(timeout = 10_000)
    public void sendUncorrectedDataShouldFailed(TestContext context) {
        final Async async = context.async();

        String uncorrectedMessage = "uncorrected json";

        vertx.eventBus().send(ROUTER_CHAT.getPath(), uncorrectedMessage, answer -> {
            context.assertTrue(answer.failed());
            context.assertEquals("Wrong data for router: " + uncorrectedMessage, answer.cause().getMessage());
            async.complete();
        });
    }

    @Test(timeout = 10_000)
    public void sendCorrectDataShouldSuccess(TestContext context) {
        final Async async = context.async();

        vertx.eventBus().send(ROUTER_CHAT.getPath(), correctGenericMessage, answer -> {
            context.assertTrue(answer.succeeded());
            async.complete();
        });
    }
}
