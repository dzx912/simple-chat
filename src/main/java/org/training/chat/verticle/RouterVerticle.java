package org.training.chat.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;

import static org.training.chat.constants.BusEndpoints.ROUTER;
import static org.training.chat.constants.BusEndpoints.TOKEN;

/**
 * Actor для маршрутизации обработки сообщений от клиента
 */
public class RouterVerticle extends AbstractVerticle {

    @Override
    public void start() {
        vertx.eventBus().localConsumer(ROUTER.getPath(), this::router);
    }

    private void router(Message<String> data) {
        final org.training.chat.data.Message message = Json.decodeValue(data.body(), org.training.chat.data.Message.class);
        System.out.println("WebSocket message.text: " + message.getText());

        String token = String.format(TOKEN.getPath(), message.getAuthor().getId());

        vertx.eventBus().send(token, data.body());

        data.reply("ok");
    }
}
