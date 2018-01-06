package org.training.chat.constants;

/**
 * Опции для запуска сервера
 */
public class ServerOption {
    private ServerOption(){}

    private static final String host = "localhost";
    private static final int port = 8080;

    public static String getHost() {
        return host;
    }

    public static int getPort() {
        return port;
    }
}
