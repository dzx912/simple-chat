package org.training.chat.verticle;

import io.vertx.core.AsyncResult;
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
import org.training.chat.data.UserDto;

import static org.training.chat.constants.BusEndpoints.DB_FIND_USER;
import static org.training.chat.constants.BusEndpoints.VALIDATE_TOKEN;

/**
 * Unit test for actor Validate token
 */
@RunWith(VertxUnitRunner.class)
public class ValidateTokenVerticleTest {

    private Vertx vertx;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();

        vertx.eventBus().registerDefaultCodec(UserDto.class, new Codec<>(UserDto.class));

        vertx.deployVerticle(ValidateTokenVerticle.class.getName(), context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test(timeout = 10_000)
    public void sendCorrectDataShouldLaunchDatabase(TestContext context) {
        final Async async = context.async();

        String expectedToken = "12345";

        vertx.eventBus().localConsumer(DB_FIND_USER.getPath(), (Message<String> tokenData) -> {
            String actualToken = tokenData.body();

            context.assertEquals(expectedToken, actualToken);
            async.complete();
        });

        String correctUrl = "/token/" + expectedToken;
        vertx.eventBus().send(VALIDATE_TOKEN.getPath(), correctUrl);
    }

    @Test(timeout = 10_000)
    public void sendSomeTrashShouldReturnFail(TestContext context) {
        final Async async = context.async();

        String badData = "ABCDEF";
        String errorMessage = "Wrong connect token, correct format: /token/ADDRESS" +
                ", you write: " + badData;
        vertx.eventBus().send(VALIDATE_TOKEN.getPath(), badData, answer -> {
            context.assertTrue(answer.failed());
            context.assertEquals(errorMessage, answer.cause().getMessage());
            async.complete();
        });
    }

    @Test(timeout = 10_000)
    public void tokenWithUserInDatabaseShouldReturnUser(TestContext context) {
        final Async async = context.async();

        UserDto expectedUser = new UserDto("id", "login", "firstName", "lastName");

        vertx.eventBus().localConsumer(DB_FIND_USER.getPath(),
                data -> data.reply(expectedUser)
        );

        String correctUrl = "/token/12345";
        vertx.eventBus().send(VALIDATE_TOKEN.getPath(), correctUrl, (AsyncResult<Message<UserDto>> resultUser) -> {
            context.assertTrue(resultUser.succeeded());

            UserDto actualUser = resultUser.result().body();
            context.assertEquals(expectedUser, actualUser);

            async.complete();
        });
    }

    @Test(timeout = 10_000)
    public void tokenWithoutUserInDatabaseShouldReturnFail(TestContext context) {
        final Async async = context.async();

        String errorMessage = "User not found";
        vertx.eventBus().localConsumer(DB_FIND_USER.getPath(),
                data -> data.fail(-2, errorMessage)
        );

        String correctUrl = "/token/12345";
        vertx.eventBus().send(VALIDATE_TOKEN.getPath(), correctUrl, (AsyncResult<Message<UserDto>> resultUser) -> {
            context.assertTrue(resultUser.failed());

            context.assertEquals(errorMessage, resultUser.cause().getMessage());

            async.complete();
        });
    }
}