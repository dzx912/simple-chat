package org.training.chat.verticle;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.training.chat.constants.BusEndpoints.ROUTER;

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
    public void testSimpleRouter(TestContext context) {
        final Async async = context.async();

        String text = "checkText";
        vertx.eventBus().localConsumer(text, receiveResult -> {
            context.assertEquals("hello + " + text, receiveResult.body());
            async.complete();
        });

        vertx.eventBus().send(ROUTER.getPath(), text);
    }
}
