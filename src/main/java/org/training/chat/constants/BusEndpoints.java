package org.training.chat.constants;

/**
 * Адреса EventBus
 */
public enum BusEndpoints {
    ROUTER("/router"),
    TOKEN("/token/%s"),
    VALIDATE_TOKEN("/validate/token"),
    GENERATE_COMMON_MESSAGE("/message/generate/common"),
    DB_SAVE_MESSAGE("db/save/message"),
    DB_LOAD_MESSAGES_BY_CHAT("db/load/messages/by/chat");

    private String path;

    BusEndpoints(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
