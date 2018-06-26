package org.training.chat.data;

import io.vertx.core.json.Json;

import java.util.Objects;

/**
 * POJO объект, хранящий информацию о пользователях
 * Передается как вложенный объект в отправляемом сообщении
 */
public class UserDto {
    private String id;
    private String login;
    private String firstName;
    private String lastName;

    public UserDto() {
    }

    public UserDto(String id, String login, String firstName, String lastName) {
        this.id = id;
        this.login = login;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDto user = (UserDto) o;
        return Objects.equals(id, user.id) &&
                Objects.equals(login, user.login) &&
                Objects.equals(firstName, user.firstName) &&
                Objects.equals(lastName, user.lastName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, login, firstName, lastName);
    }

    @Override
    public String toString() {
        return Json.encode(this);
    }
}
