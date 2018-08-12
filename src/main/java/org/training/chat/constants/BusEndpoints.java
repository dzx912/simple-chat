package org.training.chat.constants;

/**
 * Адреса EventBus
 */
public enum BusEndpoints {
    ROUTER_CHAT("/router/chat"),
    ROUTER_METHOD("/router/method"),
    TOKEN("/token/%s"),
    CHAT("/chat/%s"),
    VALIDATE_TOKEN("/validate/token"),

    CHAT_CREATE("/chat/create"),
    CHAT_ACKNOWLEDGE("/chat/acknowledge"),

    SEND_HISTORY("/message/send/history"),

    DB_SAVE_MESSAGE("db/save/message"),
    DB_LOAD_MESSAGES_BY_CHAT("db/load/messages/by/chat"),
    DB_REGISTER_USER("db/register/user"),
    DB_FIND_USER("db/find/user"),
    DB_FIND_TOKEN_BY_USER("db/find/token/by/login"),
    DB_CHAT_CREATE_BY_LOGIN("db/chat/create/by/login"),
    DB_CHAT_FIND_BY_LOGIN("db/chat/find/by/login");

    private String path;

    BusEndpoints(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
