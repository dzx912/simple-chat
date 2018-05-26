package org.training.chat.verticle;

import io.vertx.core.Vertx;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.http.WebSocketFrame;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.training.chat.constants.ServerOption;

import static org.training.chat.constants.BusEndpoints.ROUTER;

/**
 * Unit test for actor Receive WebSocket
 */
@RunWith(VertxUnitRunner.class)
public class WsServerVerticleTest {

    private final static String WEB_SOCKET_CLOSE = "\u0003�";
    private final static String CHECK_TEXT = "checkText";
    private final Logger logger = LogManager.getLogger(WsServerVerticleTest.class);
    private Vertx vertx;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();

        vertx.deployVerticle(WsServerVerticle.class.getName(), context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testWebSocketSend(TestContext context) {
        final Async async = context.async();

        vertx.eventBus().localConsumer(ROUTER.getPath(), receiveResult -> {
            context.assertEquals(CHECK_TEXT, receiveResult.body());
            async.complete();
        });

        RequestOptions options = getWSRequestOptions("1");
        vertx.createHttpClient()
                .websocketStream(options)
                .handler(ws -> ws.writeFinalTextFrame(CHECK_TEXT));
    }

    @Test
    public void testWebSocketSendReceive(TestContext context) throws InterruptedException {
        final Async async = context.async();


        RequestOptions options = getWSRequestOptions("2");

        vertx.createHttpClient().websocketStream(options).handler(
                ws -> ws.frameHandler(wsf -> receiveText(context, async, wsf)
                ));

        Thread.sleep(500);

        vertx.eventBus().send("/token/2", CHECK_TEXT);
    }

    private void receiveText(TestContext context, Async async, WebSocketFrame wsf) {
        String actualText = wsf.textData();
        logger.info("Actual receiver text: " + actualText);
        boolean isMainText = CHECK_TEXT.equals(actualText);
        boolean isCloseText = WEB_SOCKET_CLOSE.equals(actualText);
        boolean textEquals = isMainText || isCloseText;

        context.assertTrue(textEquals);

        if (isMainText) {
            async.complete();
        }
    }

    private RequestOptions getWSRequestOptions(String token) {
        RequestOptions options = new RequestOptions();
        options.setHost(ServerOption.getHost());
        options.setPort(ServerOption.getPort());
        options.setURI("/token/" + token);
        return options;
    }
}
