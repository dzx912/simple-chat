package org.training.chat.constants;

/**
 * Опции для запуска сервера
 */
public class ServerOption {
    private static final String host = "localhost";
    private static final int port = 8080;
    private static final int staticServerPort = 8081;
    private ServerOption() {
    }

    public static String getHost() {
        return host;
    }

    public static int getPort() {
        return port;
    }

    public static int getStaticServerPort() {
        return staticServerPort;
    }
}
