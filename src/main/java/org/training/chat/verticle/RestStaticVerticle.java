package org.training.chat.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.StaticHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.training.chat.constants.ServerOption;

/**
 * HTTP сервер, который раздает файлы (статический контент)
 */
public class RestStaticVerticle extends AbstractVerticle {

    private final Logger logger = LogManager.getLogger(RestStaticVerticle.class);

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

        httpServer.listen(ServerOption.getStaticServerPort());
        logger.debug("Deploy " + RestStaticVerticle.class);
    }
}
