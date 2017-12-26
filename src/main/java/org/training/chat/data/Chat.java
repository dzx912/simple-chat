package org.training.chat.data;

/**
 * DTO для работы с сущностью чата переписки
 */
public class Chat {
    private Long id;

    public Chat() {
    }

    public Chat(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
