package org.training.chat.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.StaticHandler;

/**
 */
public class RestStaticVerticle extends AbstractVerticle {

    @Override
    public void start() {
        HttpServer httpServer = vertx.createHttpServer();

        Router httpRouter = Router.router(vertx);

        httpRouter.route("/*")
                .handler(StaticHandler.create()
                        .setCachingEnabled(false)
                        .setWebRoot("static")
                );
        httpRouter.route().failureHandler(ErrorHandler.create(true));

        httpServer.requestHandler(httpRouter::accept);

        httpServer.listen(8081);
    }
}
