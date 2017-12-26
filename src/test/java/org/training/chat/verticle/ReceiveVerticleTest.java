package org.training.chat.verticle;

import io.vertx.core.Vertx;
import io.vertx.core.http.RequestOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.training.chat.constants.BusEndpoints.ROUTER;

/**
 * Unit test for actor Receive WebSocket
 */
@RunWith(VertxUnitRunner.class)
public class ReceiveVerticleTest {

    private Vertx vertx;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();

        vertx.deployVerticle(ReceiveVerticle.class.getName(), context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testWebSocketSend(TestContext context) {
        final Async async = context.async();

        String text = "checkText";

        vertx.eventBus().localConsumer(ROUTER.getPath(), receiveResult -> {
            context.assertEquals(text, receiveResult.body());
            async.complete();
        });

        RequestOptions options = getWSRequestOptions("1");
        vertx.createHttpClient()
                .websocketStream(options)
                .handler(ws -> ws.writeFinalTextFrame(text));
    }

    @Test
    public void testWebSocketSendReceive(TestContext context) throws InterruptedException {
        final Async async = context.async();

        String text = "checkText";

        RequestOptions options = getWSRequestOptions("2");

        vertx.createHttpClient().websocketStream(options).handler(
                ws -> ws.frameHandler(wsf -> {
                    context.assertEquals("Echo message: " + text, wsf.textData());
                    async.complete();
                }));

        Thread.sleep(500);

        vertx.eventBus().send("/token/2", text);
    }

    private RequestOptions getWSRequestOptions(String token) {
        RequestOptions options = new RequestOptions();
        options.setHost("localhost");
        options.setPort(8080);
        options.setURI("/token/" + token);
        return options;
    }
}
