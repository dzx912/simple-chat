package org.training.chat.verticle;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.training.chat.codec.Codec;
import org.training.chat.constants.ServerOption;
import org.training.chat.data.RequestAuthorization;
import org.training.chat.data.db.User;

import static org.training.chat.constants.BusEndpoints.DB_REGISTER_USER;

/**
 * Unit test for HTTP server
 */
@RunWith(VertxUnitRunner.class)
public class RestServerVerticleTest {

    private Vertx vertx;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();

        vertx.eventBus().registerDefaultCodec(RequestAuthorization.class, new Codec<>(RequestAuthorization.class));
        vertx.eventBus().registerDefaultCodec(User.class, new Codec<>(User.class));

        vertx.deployVerticle(RestServerVerticle.class.getName(), context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test(timeout = 10_000)
    public void sendCorrectDataShouldLaunchDatabase(TestContext context) {
        final Async async = context.async();

        String login = "dzx912";
        String firstName = "Anton";
        String lastName = "Lenok";
        String jsonRequestExpected = String.format("{\"login\":\"%s\",\"firstName\":\"%s\",\"lastName\":\"%s\"}",
                login, firstName, lastName);

        vertx.eventBus().localConsumer(DB_REGISTER_USER.getPath(), (Message<RequestAuthorization> requestAuthorizationData) -> {
            RequestAuthorization jsonRequestActual = requestAuthorizationData.body();

            context.assertEquals(login, jsonRequestActual.getLogin());
            context.assertEquals(firstName, jsonRequestActual.getFirstName());
            context.assertEquals(lastName, jsonRequestActual.getLastName());
            async.complete();
        });

        vertx.createHttpClient().post(ServerOption.getHttpPort(), "localhost", "/sign-up", a -> {
        }).end(jsonRequestExpected);
    }

    @Test(timeout = 10_000)
    public void correctDataShouldReturnToken(TestContext context) {
        final Async async = context.async();

        String login = "dzx912";
        String firstName = "Anton";
        String lastName = "Lenok";
        String jsonRequestExpected = String.format("{\"login\":\"%s\",\"firstName\":\"%s\",\"lastName\":\"%s\"}",
                login, firstName, lastName);
        User user = new User(new RequestAuthorization(login, firstName, lastName));

        vertx.eventBus().localConsumer(DB_REGISTER_USER.getPath(),
                data -> data.reply(user)
        );

        vertx.createHttpClient().post(ServerOption.getHttpPort(), "localhost", "/sign-up").handler(answer ->
                answer.bodyHandler(body -> {
                    String actual = body.toString();
                    context.assertEquals(user.getToken(), actual);

                    async.complete();
                })
        ).end(jsonRequestExpected);
    }

    @Test(timeout = 10_000)
    public void sendSomeTrashShouldReturnFail(TestContext context) {
        final Async async = context.async();

        String badData = "ABCDEF";
        String errorMessage = String.format("Uncorrect JSON authorization, must be like this: %s\nAnd you send: %s",
                "{\"login\":\"nick\",\"firstName\":\"Tom\",\"lastName\":\"Tyler\"}",
                badData);

        vertx.createHttpClient().post(ServerOption.getHttpPort(), "localhost", "/sign-up").handler(answer -> {
            context.assertEquals(500, answer.statusCode());
            answer.bodyHandler(body -> {
                String actual = body.toString();
                context.assertEquals(errorMessage, actual);

                async.complete();
            });
        }).end(badData);
    }
}