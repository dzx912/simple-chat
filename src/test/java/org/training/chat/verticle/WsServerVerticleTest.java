package org.training.chat.verticle;

import io.vertx.core.Vertx;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.http.WebSocketFrame;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
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
import org.training.chat.constants.ServerOption;
import org.training.chat.data.*;

import static org.training.chat.constants.BusEndpoints.*;

/**
 * Unit test for actor Receive WebSocket
 */
@RunWith(VertxUnitRunner.class)
public class WsServerVerticleTest {

    private final static String WEB_SOCKET_CLOSE = "\u0003�";
    private final static String TEXT_HISTORY = "{\"history\":[]}";
    private final static String CHECK_TEXT = "checkText";
    private final Logger logger = LogManager.getLogger(WsServerVerticleTest.class);
    private Vertx vertx;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();

        vertx.eventBus().registerDefaultCodec(TextMessage.class, new CommonMessageCodec<>(TextMessage.class));
        vertx.eventBus().registerDefaultCodec(Chat.class, new CommonMessageCodec<>(Chat.class));

        vertx.deployVerticle(WsServerVerticle.class.getName(), context.asyncAssertSuccess());

        deployCommonConsumer();
    }

    private void deployCommonConsumer() {
        vertx.eventBus().localConsumer(VALIDATE_TOKEN.getPath(),
                data -> data.reply(data.body())
        );
        vertx.eventBus().localConsumer(DB_SAVE_MESSAGE.getPath(),
                data -> data.reply("1")
        );
        vertx.eventBus().localConsumer(DB_LOAD_MESSAGES_BY_CHAT.getPath(),
                data -> data.reply("[]")
        );
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test(timeout = 10_000)
    public void testWebSocketSend(TestContext context) {
        final Async async = context.async();

        String token = "1";
        TempMessage tempMessage = new TempMessage(token, CHECK_TEXT);
        String json = Json.encode(tempMessage);

        vertx.eventBus().localConsumer(GENERATE_COMMON_MESSAGE.getPath(), receiveResult -> {
            context.assertEquals(json, receiveResult.body());
            async.complete();
        });

        RequestOptions options = getWSRequestOptions(token);
        vertx.createHttpClient()
                .websocketStream(options)
                .handler(ws -> ws.writeFinalTextFrame(CHECK_TEXT));
    }

    @Test(timeout = 10_000)
    public void testWebSocketSendReceive(TestContext context) throws InterruptedException {
        final Async async = context.async();

        Long idChat = 2L;

        RequestMessage requestMessage =
                new RequestMessage(2L, "text message", new Chat(idChat));
        TextMessage correctMessage = new TextMessage(
                new User(1L),
                2L,
                "text message",
                3L,
                10L
        );

        RequestOptions options = getWSRequestOptions(idChat.toString());

        vertx.createHttpClient().websocketStream(options).handler(
                ws -> ws.frameHandler(wsf -> receiveText(context, async, wsf, correctMessage)
                ));

        Thread.sleep(500);

        vertx.eventBus().send("/token/2", correctMessage);
    }

    private void receiveText(TestContext context, Async async, WebSocketFrame wsf, TextMessage correctMessage) {
        String jsonCommonMessage = wsf.textData();
        boolean isCloseText = WEB_SOCKET_CLOSE.equals(jsonCommonMessage);
        boolean isHistory = TEXT_HISTORY.equals(jsonCommonMessage);
        if (!isCloseText && !isHistory) {
            try {
                TextMessage actualMessage = Json.decodeValue(jsonCommonMessage, TextMessage.class);
                logger.info("Actual receiver text: " + actualMessage);

                context.assertEquals(correctMessage, actualMessage);
                async.complete();
            } catch (DecodeException e) {
                logger.error(e.toString());
                context.fail();
            }
        }
    }

    @Test(timeout = 10_000)
    public void afterOpenWsShouldGetHistory(TestContext context) {
        final Async async = context.async();

        RequestOptions options = getWSRequestOptions("3");

        vertx.createHttpClient().websocketStream(options).handler(
                ws -> ws.frameHandler(wsf -> checkHistory(context, async, wsf)
                ));
    }

    private void checkHistory(TestContext context, Async async, WebSocketFrame wsf) {
        String message = wsf.textData();
        boolean isCloseText = WEB_SOCKET_CLOSE.equals(message);
        if (!isCloseText) {
            context.assertEquals(TEXT_HISTORY, message);
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
