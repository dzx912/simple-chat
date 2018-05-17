package org.training.chat.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.DecodeException;
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
        try {
            String jsonText = data.body();
            if (jsonText.isEmpty()) {
                data.fail(-2, "Empty json");
                return;
            }
            final org.training.chat.data.Message message = Json.decodeValue(jsonText, org.training.chat.data.Message.class);
            System.out.println("WebSocket message.text: " + message.getText());

            String token = String.format(TOKEN.getPath(), message.getChat().getId());

            vertx.eventBus().send(token, jsonText);

            data.reply("ok");
        } catch (DecodeException exception) {
            System.out.println("Wrong data for router: " + exception);
            data.fail(-1, exception.getMessage());
        }
    }
}
