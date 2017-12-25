package org.training.chat.constants;

/**
 * Адреса EventBus
 */
public enum BusEndpoints {
    ROUTER("/router"), TOKEN("/token%s");

    private String path;

    BusEndpoints(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
