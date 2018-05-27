package org.training.chat.data;

import java.util.Objects;

/**
 * Метаданные о сообщении (автор, дата приема)
 * Полезная информация о сообщении, которая дабавляется на сервере
 */
public class Metadata {
    private User author;
    private Long timestamp;

    public Metadata() {
    }

    public Metadata(User author, Long timestamp) {
        this.author = author;
        this.timestamp = timestamp;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Metadata metadata = (Metadata) o;
        return Objects.equals(author, metadata.author) &&
                Objects.equals(timestamp, metadata.timestamp);
    }

    @Override
    public int hashCode() {

        return Objects.hash(author, timestamp);
    }
}
