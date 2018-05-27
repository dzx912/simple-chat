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
import org.training.chat.codec.CommonMessageCodec;
import org.training.chat.data.CommonMessage;
import org.training.chat.integration.client.WSClient;
import org.training.chat.verticle.MetadataVerticle;
import org.training.chat.verticle.RouterVerticle;
import org.training.chat.verticle.ValidateTokenVerticle;
import org.training.chat.verticle.WsServerVerticle;

/**
 * Интеграционный тест, проверяющий отправку и доставку сообщений
 */
@RunWith(VertxUnitRunner.class)
public class SendTextMessageTest {
    private final static String WEB_SOCKET_CLOSE = "\u0003�";


    private final Logger logger = LogManager.getLogger(SendTextMessageTest.class);

    private Vertx vertx;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();

        vertx.eventBus().registerDefaultCodec(CommonMessage.class, new CommonMessageCodec());

        vertx.deployVerticle(WsServerVerticle.class.getName(), context.asyncAssertSuccess());
        vertx.deployVerticle(RouterVerticle.class.getName(), context.asyncAssertSuccess());
        vertx.deployVerticle(ValidateTokenVerticle.class.getName(), context.asyncAssertSuccess());
        vertx.deployVerticle(MetadataVerticle.class.getName(), context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test(timeout = 10_000)
    public void testSendTextMessage(TestContext context) {
        final Async async = context.async();

        WSClient client1 = new WSClient(vertx, "1");
        WSClient client2 = new WSClient(vertx, "2");

        String text = "{\"clientId\":1,\"text\":\"hello\",\"chat\":{\"id\":2}}";
        String startExpected = "{\"metadata\":{\"author\":{\"id\":1},\"timestamp\":";
        //1234567890123
        String finishExpected = "},\"message\":{\"clientId\":1,\"text\":\"hello\",\"chat\":{\"id\":2}}}";
        client1.setSendText(text);
        client2.setHandler(receiveText -> {
            logger.info("Receive text: " + receiveText);
            boolean isCloseWebSocket = WEB_SOCKET_CLOSE.equals(receiveText);
            if (!isCloseWebSocket) {
                context.assertEquals(startExpected, receiveText.substring(0, startExpected.length()));
                int lengthTimestamp = 13;
                context.assertEquals(finishExpected, receiveText.substring(startExpected.length() + lengthTimestamp));

                client2.close();
                client1.close();

                async.complete();
            }
        });

        client1.run();
        client2.run();
    }
}
