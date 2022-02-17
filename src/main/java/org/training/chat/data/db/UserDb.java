package org.training.chat.data.db;

import org.training.chat.data.RequestAuthorization;

import java.util.Random;

/**
 * POJO объект, который хранит данные о пользователе в БД
 */
public record UserDb(String login, String firstName, String lastName, String token) {

    public static UserDb of(RequestAuthorization user) {
        long randomToken = new Random().nextLong();
        return new UserDb(
                user.login(),
                user.firstName(),
                user.lastName(),
                String.valueOf(randomToken));
    }
}
