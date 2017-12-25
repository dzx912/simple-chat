package org.training.chat.data;

/**
 * POJO объект, хранящий информацию о пользователях, которые создают сообщения
 */
public class User {
    private Long id;

    public User() {
    }

    public User(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
