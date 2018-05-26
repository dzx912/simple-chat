package org.training.chat.integration.test;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.training.chat.integration.client.WSClient;
import org.training.chat.verticle.WsServerVerticle;
import org.training.chat.verticle.RouterVerticle;

/**
 * Интеграционный тест, проверяющий отправку и доставку сообщений
 */
@RunWith(VertxUnitRunner.class)
public class SendMessageTest {

    private Vertx vertx;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();

        vertx.deployVerticle(WsServerVerticle.class.getName(), context.asyncAssertSuccess());
        vertx.deployVerticle(RouterVerticle.class.getName(), context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testSendTextMessage(TestContext context) {
        final Async async = context.async();

        WSClient client1 = new WSClient(vertx, "1");
        WSClient client2 = new WSClient(vertx, "2");

        String text = "{\"id\":1,\"text\":\"hello\",\"author\":{\"id\":1},\"chat\":{\"id\":2}}";
        client1.setSendText(text);
        client2.setHandler(receiveText -> {
            System.out.println("Receive text: " + receiveText);
            context.assertEquals(text, receiveText);

            client1.close();
            client2.close();

            async.complete();
        });

        client1.run();
        client2.run();
    }
}
