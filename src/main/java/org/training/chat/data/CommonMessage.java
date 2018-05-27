package org.training.chat.data;

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
}
