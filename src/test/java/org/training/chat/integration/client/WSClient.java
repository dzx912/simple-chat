package org.training.chat.integration.client;

import com.google.common.base.Strings;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebSocketFrame;
import org.training.chat.constants.ServerOption;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Тестовый клиент, для интеграционных тестов
 */
public class WSClient {
    private final String token;
    private final Vertx vertx;
    private final List<Consumer<String>> handlers;
    private final List<String> sendText;

    private WebSocket webSocket;

    public WSClient(Vertx vertx, String token) {
        if (vertx == null) {
            throw new IllegalArgumentException("Vertx is null");

        }
        if (Strings.isNullOrEmpty(token)) {
            throw new IllegalArgumentException("Token is empty");
        }
        this.vertx = vertx;
        this.token = token;
        handlers = new ArrayList<>();
        sendText = new ArrayList<>();
    }

    public void setHandler(Consumer<String> handler) {
        this.handlers.add(handler);
    }

    public void setSendText(String text) {
        sendText.add(text);
    }

    public void run() {
        HttpClient httpClient = vertx.createHttpClient();
        httpClient.websocketStream(getWSRequestOptions())
                .handler(this::wsConnect);
    }

    public void close() {
        webSocket.close();
    }


    private void wsConnect(WebSocket webSocket) {
        this.webSocket = webSocket;
        webSocket.frameHandler(this::wsGetMessage);

        sendText.forEach(webSocket::writeFinalTextFrame);
    }

    private void wsGetMessage(WebSocketFrame webSocketFrame) {
        handlers.forEach(
                handler -> handler.accept(webSocketFrame.textData()
                ));
    }

    private RequestOptions getWSRequestOptions() {
        RequestOptions options = new RequestOptions();
        options.setHost(ServerOption.getHost());
        options.setPort(ServerOption.getPort());
        options.setURI("/token/" + token);
        return options;
    }
}
