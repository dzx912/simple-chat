package org.training.chat.data;

/**
 * POJO объект, хранящий сообщение от клиента
 */
public class TextMessage {

    private Long clientId;
    private String text;
    private Chat chat;

    public TextMessage() {
    }

    public TextMessage(Long clientId, String text, Chat chat) {
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
}
