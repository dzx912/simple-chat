package org.training.chat.constants;

/**
 * Адреса EventBus
 */
public enum BusEndpoints {
    ROUTER("/router"),
    TOKEN("/token/%s"),
    VALIDATE_TOKEN("/validate/token"),
    GENERATE_COMMON_MESSAGE("/message/generate/common");

    private String path;

    BusEndpoints(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
