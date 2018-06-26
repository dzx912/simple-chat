package org.training.chat.constants;

/**
 * Опции для запуска сервера
 */
public class ServerOption {
    private static final String host = "localhost";
    private static final int httpPort = 8080;
    private static final int wsPort = 8081;

    private ServerOption() {
    }

    public static String getHost() {
        return host;
    }

    public static int getWsPort() {
        return wsPort;
    }

    public static int getHttpPort() {
        return httpPort;
    }

}
