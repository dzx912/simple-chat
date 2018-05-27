package org.training.chat.data;

import java.time.LocalDateTime;

/**
 * Метаданные о сообщении (автор, дата приема)
 * Полезная информация о сообщении, которая дабавляется на сервере
 */
public class Metadata {
    private User author;
    private LocalDateTime data;

    public Metadata(User author, LocalDateTime data) {
        this.author = author;
        this.data = data;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }
}
