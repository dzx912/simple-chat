package org.training.chat;

import io.vertx.core.Vertx;
import org.training.chat.verticle.ReceiveVerticle;
import org.training.chat.verticle.RestStaticVerticle;
import org.training.chat.verticle.RouterVerticle;

public class MainApp {

    public static void main(String args[]) {
        Vertx vertx = Vertx.vertx();

        vertx.deployVerticle(new ReceiveVerticle());
        vertx.deployVerticle(new RouterVerticle());
        vertx.deployVerticle(new RestStaticVerticle());
    }
}
