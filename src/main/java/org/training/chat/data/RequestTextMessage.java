package org.training.chat.data;

/**
 * POJO объект, принимающий текстовое сообщение от клиента
 */
public record RequestTextMessage(Long clientId, String text, String chatId) {
}
