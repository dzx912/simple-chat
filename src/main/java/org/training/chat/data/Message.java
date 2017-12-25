package org.training.chat.data;

/**
 * POJO объект, хранящий основное сообщение от клиента
 */
public class Message {

    private Long id;
    private String text;
    private User author;

    public Message() {
    }

    public Message(Long id, String text, User author) {
        this.id = id;
        this.text = text;
        this.author = author;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }
}
