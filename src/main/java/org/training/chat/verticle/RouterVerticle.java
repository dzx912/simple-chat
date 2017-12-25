package org.training.chat.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;

import static org.training.chat.constants.BusEndpoints.ROUTER;

/**
 * Actor для маршрутизации обработки сообщений от клиента
 */
public class RouterVerticle extends AbstractVerticle {

    @Override
    public void start() {
        vertx.eventBus().localConsumer(ROUTER.getPath(), this::router);
    }

    private void router(Message<String> data) {
        String message = data.body();
        System.out.println("WebSocket message: " + message);
        vertx.eventBus().send(message, "hello + " + message);
        data.reply("ok");
    }
}
