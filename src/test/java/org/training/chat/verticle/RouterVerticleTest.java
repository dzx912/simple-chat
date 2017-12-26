package org.training.chat.verticle;

import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.training.chat.data.Chat;
import org.training.chat.data.Message;
import org.training.chat.data.User;

import static org.training.chat.constants.BusEndpoints.ROUTER;
import static org.training.chat.constants.BusEndpoints.TOKEN;

/**
 * Unit test for actor Router
 */
@RunWith(VertxUnitRunner.class)
public class RouterVerticleTest {

    private Vertx vertx;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();

        vertx.deployVerticle(RouterVerticle.class.getName(), context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testRouter(TestContext context) {
        final Async async = context.async();

        String text = "checkText";
        long idMessage = 1L;
        long idUser = 2L;
        long idChat = 3L;
        String tokenUser = String.format(TOKEN.getPath(), idChat);
        final String jsonRequest = Json.encodePrettily(
                new Message(idMessage,
                        text,
                        new User(idUser),
                        new Chat(idChat))
        );

        vertx.eventBus().localConsumer(tokenUser, receiveResult -> {
            context.assertEquals(jsonRequest, receiveResult.body());
            async.complete();
        });

        vertx.eventBus().send(ROUTER.getPath(), jsonRequest);
    }
}
