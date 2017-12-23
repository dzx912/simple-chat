package org.training.chat;

import io.vertx.core.Vertx;
import org.training.chat.verticle.ReceiveVerticle;

public class MainApp {

    public static void main(String args[]) {
        Vertx vertx = Vertx.vertx();

        vertx.deployVerticle(new ReceiveVerticle());
    }
}
