package org.training.chat.data;

import java.util.Objects;

/**
 * POJO объект, принимающий сообщение от клиента
 */
public class RequestMessage {

    private Long clientId;
    private String text;
    private String chatId;

    public RequestMessage() {
    }

    public RequestMessage(Long clientId, String text, String chatId) {
        this.clientId = clientId;
        this.text = text;
        this.chatId = chatId;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestMessage that = (RequestMessage) o;
        return Objects.equals(clientId, that.clientId) &&
                Objects.equals(text, that.text) &&
                Objects.equals(chatId, that.chatId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, text, chatId);
    }
}
