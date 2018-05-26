package org.training.chat.handler;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.ServerWebSocket;

/**
 * Handler, который отправляет в WebSocket сообщение от сервера -> клиенту
 */
public class SendMessageHandler implements Handler<Message<String>> {

    private ServerWebSocket webSocket;

    public SendMessageHandler(ServerWebSocket webSocket) {
        this.webSocket = webSocket;
    }

    @Override
    public void handle(Message<String> data) {
        String message = data.body();
        System.out.println("SendMessageHandler WebSocket message: " + message);
        webSocket.writeFinalTextFrame(message);
        data.reply("ok");
    }
}
