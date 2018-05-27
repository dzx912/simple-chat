package org.training.chat.data;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chat chat = (Chat) o;
        return Objects.equals(id, chat.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }
}
