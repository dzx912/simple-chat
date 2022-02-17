package org.training.chat.data;

/**
 * Данные, которые передаются при создании чата
 */
public record RequestCreateChat(String loginReceiver) {
}
