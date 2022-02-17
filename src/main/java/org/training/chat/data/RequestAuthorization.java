package org.training.chat.data;

/**
 * Данные о пользователе, которые передаются при регестрации
 */
public record RequestAuthorization(String login, String firstName, String lastName) {
}
