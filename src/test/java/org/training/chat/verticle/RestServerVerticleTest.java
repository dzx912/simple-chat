package org.training.chat.verticle;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.WebClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.training.chat.codec.Codec;
import org.training.chat.constants.ServerOption;
import org.training.chat.data.RequestAuthorization;
import org.training.chat.data.db.UserDb;

import static org.training.chat.constants.BusEndpoints.DB_REGISTER_USER;

/**
 * Unit test for HTTP server
 */
@RunWith(VertxUnitRunner.class)
public class RestServerVerticleTest {

    private Vertx vertx;
    private WebClient client;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();
        client = WebClient.create(vertx);

        vertx.eventBus().registerDefaultCodec(RequestAuthorization.class, new Codec<>(RequestAuthorization.class));
        vertx.eventBus().registerDefaultCodec(UserDb.class, new Codec<>(UserDb.class));

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

            context.assertEquals(login, jsonRequestActual.login());
            context.assertEquals(firstName, jsonRequestActual.firstName());
            context.assertEquals(lastName, jsonRequestActual.lastName());
            async.complete();
        });

        client.post(ServerOption.getHttpPort(), "localhost", "/sign-up")
                .sendBuffer(Buffer.buffer(jsonRequestExpected));
    }

    @Test(timeout = 10_000)
    public void correctDataShouldReturnToken(TestContext context) {
        final Async async = context.async();

        String login = "dzx912";
        String firstName = "Anton";
        String lastName = "Lenok";
        String jsonRequestExpected = String.format("{\"login\":\"%s\",\"firstName\":\"%s\",\"lastName\":\"%s\"}",
                login, firstName, lastName);
        UserDb user = UserDb.of(new RequestAuthorization(login, firstName, lastName));

        vertx.eventBus().localConsumer(DB_REGISTER_USER.getPath(),
                data -> data.reply(user)
        );

        client.post(ServerOption.getHttpPort(), "localhost", "/sign-up")
                .sendBuffer(Buffer.buffer(jsonRequestExpected))
                .onComplete(answer -> {
                    answer.result().bodyAsString();
                    context.assertEquals(user.token(), answer.result().bodyAsString());

                    async.complete();
                });
    }

    @Test(timeout = 10_000)
    public void sendSomeTrashShouldReturnFail(TestContext context) {
        final Async async = context.async();

        String badData = "ABCDEF";
        String errorMessage = String.format("Uncorrect JSON authorization, must be like this: %s\nAnd you send: %s",
                "{\"login\":\"nick\",\"firstName\":\"Tom\",\"lastName\":\"Tyler\"}",
                badData);

        client.post(ServerOption.getHttpPort(), "localhost", "/sign-up")
                .sendBuffer(Buffer.buffer(badData))
                .onComplete(answer -> {
                    context.assertEquals(500, answer.result().statusCode());
                    final String actual = answer.result().bodyAsString();
                    context.assertEquals(errorMessage, actual);

                    async.complete();
                });
    }
}