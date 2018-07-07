package org.training.chat.data;

import java.util.List;
import java.util.Objects;

/**
 * DTO для работы с сущностью чата переписки
 */
public class Chat {
    private Long id;

    private List<UserDto> users;

    public Chat() {
    }

    public Chat(Long id, List<UserDto> users) {
        this.id = id;
        this.users = users;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<UserDto> getUsers() {
        return users;
    }

    public void setUsers(List<UserDto> users) {
        this.users = users;
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
