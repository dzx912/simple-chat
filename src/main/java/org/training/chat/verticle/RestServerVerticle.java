package org.training.chat.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.StaticHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.training.chat.constants.ServerOption;
import org.training.chat.data.RequestAuthorization;
import org.training.chat.data.db.User;

import static org.training.chat.constants.BusEndpoints.DB_REGISTER_USER;

/**
 * HTTP сервер, который раздает файлы (статический контент)
 */
public class RestServerVerticle extends AbstractVerticle {

    private final Logger logger = LogManager.getLogger(RestServerVerticle.class);

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

        httpRouter.route().handler(BodyHandler.create());
        httpRouter.post("/sign-up").handler(this::signUp);

        httpServer.requestHandler(httpRouter::accept);

        httpServer.listen(ServerOption.getHttpPort());
        logger.debug("Deploy " + RestServerVerticle.class);
    }

    private void signUp(RoutingContext routingContext) {
        try {
            String signUpData = routingContext.getBodyAsString();
            RequestAuthorization requestAuthorization =
                    Json.decodeValue(signUpData, RequestAuthorization.class);

            vertx.eventBus().send(DB_REGISTER_USER.getPath(), requestAuthorization,
                    (AsyncResult<Message<User>> result) ->
                            answerRegistration(result, routingContext));
        } catch (DecodeException e) {
            logger.error(e);
            routingContext.fail(e);
        }
    }

    private void answerRegistration(AsyncResult<Message<User>> messageAsyncResult, RoutingContext routingContext) {
        if (messageAsyncResult.succeeded()) {
            String token = messageAsyncResult.result().body().getToken();
            logger.info("Register user by token: " + token);
            routingContext.response().end(token);
        } else {
            routingContext.fail(messageAsyncResult.cause());
        }
    }
}
