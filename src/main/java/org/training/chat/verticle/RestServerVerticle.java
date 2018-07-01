package org.training.chat.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
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
 * HTTP сервер, выполнеяет две функции:
 * 1 - раздает файлы (статический контент)
 * 2 - обслуживает REST методы (регистрация пользователя)
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
        String signUpData = routingContext.getBodyAsString();
        HttpServerResponse serverResponse = routingContext.response();
        try {
            RequestAuthorization requestAuthorization =
                    Json.decodeValue(signUpData, RequestAuthorization.class);

            vertx.eventBus().send(DB_REGISTER_USER.getPath(), requestAuthorization,
                    (AsyncResult<Message<User>> result) ->
                            answerRegistration(result, serverResponse));
        } catch (DecodeException e) {
            logger.error(e);
            returnErrorMessage(signUpData, serverResponse, e);
        }
    }

    private void returnErrorMessage(String signUpData, HttpServerResponse serverResponse, DecodeException e) {
        String message = String.format("Uncorrect JSON authorization, must be like this: %s\nAnd you send: %s",
                "{\"login\":\"nick\",\"firstName\":\"Tom\",\"lastName\":\"Tyler\"}",
                signUpData);
        serverResponse.setStatusCode(500);
        serverResponse.end(message);
    }

    private void answerRegistration(AsyncResult<Message<User>> messageAsyncResult, HttpServerResponse serverResponse) {
        if (messageAsyncResult.succeeded()) {
            String token = messageAsyncResult.result().body().getToken();
            logger.info("Register user by token: " + token);
            serverResponse.end(token);
        } else {
            serverResponse.setStatusCode(500);
            serverResponse.setStatusMessage(messageAsyncResult.cause().getMessage());
        }
    }
}
