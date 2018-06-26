package org.training.chat.verticle;

import io.vertx.core.Vertx;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.http.WebSocketFrame;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.training.chat.codec.Codec;
import org.training.chat.constants.ServerOption;
import org.training.chat.data.*;

import static org.training.chat.constants.BusEndpoints.*;

/**
 * Unit test for actor Receive WebSocket
 */
@RunWith(VertxUnitRunner.class)
public class WsServerVerticleTest {

    private final static String WEB_SOCKET_CLOSE = "\u0003�";
    private final static String TEXT_HISTORY = "{\"type\":\"history\",\"content\":{\"history\":[]}}";
    private final static String CHECK_TEXT = "checkText";
    private final static UserDto USER = new UserDto("1", "dzx912", "Anton", "Lenok");
    private final Logger logger = LogManager.getLogger(WsServerVerticleTest.class);
    private Vertx vertx;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();

        vertx.eventBus().registerDefaultCodec(TextMessage.class, new Codec<>(TextMessage.class));
        vertx.eventBus().registerDefaultCodec(Chat.class, new Codec<>(Chat.class));
        vertx.eventBus().registerDefaultCodec(UserDto.class, new Codec<>(UserDto.class));

        vertx.deployVerticle(WsServerVerticle.class.getName(), context.asyncAssertSuccess());

        deployCommonConsumer();
    }

    private void deployCommonConsumer() {
        vertx.eventBus().localConsumer(VALIDATE_TOKEN.getPath(),
                data -> data.reply(USER)
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
        TempMessage tempMessage = new TempMessage(USER, CHECK_TEXT);
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
                new UserDto("id", "login", "firstName", "lastName"),
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
        String message = wsf.textData();
        boolean isCloseText = WEB_SOCKET_CLOSE.equals(message);
        boolean isHistory = TEXT_HISTORY.equals(message);
        if (!isCloseText && !isHistory) {
            try {
                logger.info("Actual receiver message: " + message);
                assertTextMessage(context, correctMessage, message);
                async.complete();
            } catch (DecodeException e) {
                logger.error(e.toString());
                context.fail();
            }
        }
    }

    private void assertTextMessage(TestContext context, TextMessage correctMessage, String message) {
        JsonObject json = new JsonObject(message);

        context.assertEquals("text", json.getString("type"));

        JsonObject contentMessage = json.getJsonObject("content");
        String jsonTextMessage = Json.encode(contentMessage);
        TextMessage actualMessage = Json.decodeValue(jsonTextMessage, TextMessage.class);

        context.assertEquals(correctMessage, actualMessage);
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
        options.setPort(ServerOption.getWsPort());
        options.setURI("/token/" + token);
        return options;
    }
}
