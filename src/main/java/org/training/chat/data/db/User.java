package org.training.chat.data.db;

import org.training.chat.data.RequestAuthorization;

import java.util.Random;

/**
 * POJO объект, который хранит данные о пользователе в БД
 */
public class User {
    private String login;
    private String firstName;
    private String lastName;
    private String token;

    public User() {
    }

    public User(RequestAuthorization user) {
        this.login = user.login();
        this.firstName = user.firstName();
        this.lastName = user.lastName();

        long randomToken = new Random().nextLong();
        this.token = String.valueOf(randomToken);
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
