package org.training.chat.data;

import io.vertx.core.json.Json;

import java.util.Objects;

/**
 * Общее сообщение, с частью, полученной от клиента
 * И добавленными метаданными на сервере
 */
public class CommonMessage {

    private Metadata metadata;
    private TextMessage message;

    public CommonMessage() {
    }

    public CommonMessage(Metadata metadata, TextMessage message) {
        this.metadata = metadata;
        this.message = message;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public TextMessage getMessage() {
        return message;
    }

    public void setMessage(TextMessage message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return Json.encode(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommonMessage that = (CommonMessage) o;
        return Objects.equals(metadata, that.metadata) &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {

        return Objects.hash(metadata, message);
    }
}
