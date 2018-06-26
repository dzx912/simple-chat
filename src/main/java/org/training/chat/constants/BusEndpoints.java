package org.training.chat.constants;

/**
 * Адреса EventBus
 */
public enum BusEndpoints {
    ROUTER("/router"),
    TOKEN("/token/%s"),
    VALIDATE_TOKEN("/validate/token"),
    GENERATE_COMMON_MESSAGE("/message/generate/common"),

    SEND_HISTORY("/message/send/history"),

    DB_SAVE_MESSAGE("db/save/message"),
    DB_LOAD_MESSAGES_BY_CHAT("db/load/messages/by/chat"),
    DB_REGISTER_USER("db/register/user"),
    DB_FIND_USER("db/find/user");

    private String path;

    BusEndpoints(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
