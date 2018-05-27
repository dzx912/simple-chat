package org.training.chat;

import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.training.chat.verticle.RestStaticVerticle;
import org.training.chat.verticle.RouterVerticle;
import org.training.chat.verticle.WsServerVerticle;

public class MainApp {

    private final static Logger logger = LogManager.getLogger(MainApp.class);

    public static void main(String args[]) {
        logger.info("Start App");
        Vertx vertx = Vertx.vertx();

        vertx.deployVerticle(new WsServerVerticle());
        vertx.deployVerticle(new RouterVerticle());
        vertx.deployVerticle(new RestStaticVerticle());
    }
}
