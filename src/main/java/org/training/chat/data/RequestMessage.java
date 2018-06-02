package org.training.chat.data;

import java.util.Objects;

/**
 * POJO объект, хранящий сообщение от клиента
 */
public class RequestMessage {

    private Long clientId;
    private String text;
    private Chat chat;

    public RequestMessage() {
    }

    public RequestMessage(Long clientId, String text, Chat chat) {
        this.clientId = clientId;
        this.text = text;
        this.chat = chat;
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

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestMessage that = (RequestMessage) o;
        return Objects.equals(clientId, that.clientId) &&
                Objects.equals(text, that.text) &&
                Objects.equals(chat, that.chat);
    }

    @Override
    public int hashCode() {

        return Objects.hash(clientId, text, chat);
    }
}
