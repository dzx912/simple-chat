package org.training.chat.verticle;

import io.vertx.core.Vertx;
import io.vertx.core.http.WebSocketConnectOptions;
import io.vertx.core.http.WebSocketFrame;
import io.vertx.core.json.DecodeException;
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
import org.training.chat.data.Chat;
import org.training.chat.data.TempMessage;
import org.training.chat.data.TextMessage;
import org.training.chat.data.User;
import org.training.chat.util.Answerer;

import java.util.Objects;

import static org.training.chat.constants.BusEndpoints.*;

/**
 * Unit test for actor Receive WebSocket
 */
@RunWith(VertxUnitRunner.class)
public class WsServerVerticleTest {

    private final static String WEB_SOCKET_CLOSE = "\u0003�";
    private final static String TEXT_HISTORY = "{\"type\":\"history\",\"content\":{\"history\":[]}}";
    private final static String CHECK_TEXT = "checkText";
    private final static User USER = new User("1", "dzx912", "Anton", "Lenok");
    private final Logger logger = LogManager.getLogger(WsServerVerticleTest.class);
    private Vertx vertx;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();

        vertx.eventBus().registerDefaultCodec(TempMessage.class, new Codec<>(TempMessage.class));
        vertx.eventBus().registerDefaultCodec(TextMessage.class, new Codec<>(TextMessage.class));
        vertx.eventBus().registerDefaultCodec(Chat.class, new Codec<>(Chat.class));
        vertx.eventBus().registerDefaultCodec(User.class, new Codec<>(User.class));

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

        vertx.eventBus().localConsumer(ROUTER_METHOD.getPath(), receiveResult -> {
            if (Objects.equals(tempMessage, receiveResult.body())) {
                async.complete();
            }
        });

        WebSocketConnectOptions options = getWSRequestOptions(token);
        vertx.createHttpClient()
                .webSocket(options)
                .onSuccess(ws -> ws.writeFinalTextFrame(CHECK_TEXT));
    }

    @Test(timeout = 10_000)
    public void testWebSocketSendReceive(TestContext context) throws InterruptedException {
        final Async async = context.async();

        String idChat = "2";

        TextMessage textMessage = new TextMessage(
                new User("id", "login", "firstName", "lastName"),
                "2",
                "text message",
                3L,
                10L
        );
        String correctMessage = Answerer.createResponseMessage("text", textMessage);

        WebSocketConnectOptions options = getWSRequestOptions(idChat);

        vertx.createHttpClient().webSocket(options).onSuccess(
                ws -> ws.frameHandler(wsf -> receiveText(context, async, wsf, correctMessage)
                ));

        Thread.sleep(500);

        vertx.eventBus().send("/token/2", correctMessage);
    }

    private void receiveText(TestContext context, Async async, WebSocketFrame wsf, String correctMessage) {
        String message = wsf.textData();
        boolean isCloseText = WEB_SOCKET_CLOSE.equals(message);
        boolean isHistory = TEXT_HISTORY.equals(message);
        if (!isCloseText && !isHistory) {
            try {
                logger.info("Actual receiver message: " + message);
                context.assertEquals(correctMessage, message);
                async.complete();
            } catch (DecodeException e) {
                logger.error(e.toString());
                context.fail();
            }
        }
    }

    private void checkHistory(TestContext context, Async async, WebSocketFrame wsf) {
        String message = wsf.textData();
        boolean isCloseText = WEB_SOCKET_CLOSE.equals(message);
        if (!isCloseText) {
            context.assertEquals(TEXT_HISTORY, message);
            async.complete();
        }
    }

    private WebSocketConnectOptions getWSRequestOptions(String token) {
        WebSocketConnectOptions options = new WebSocketConnectOptions();
        options.setHost(ServerOption.getHost());
        options.setPort(ServerOption.getWsPort());
        options.setURI("/token/" + token);
        return options;
    }
}
