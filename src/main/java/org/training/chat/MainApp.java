package org.training.chat;

import io.vertx.core.Vertx;
import org.training.chat.verticle.ChatVerticle;

public class MainApp {

    public static void main(String args[]) {
        Vertx vertx = Vertx.vertx();

        vertx.deployVerticle(new ChatVerticle());
    }
}
