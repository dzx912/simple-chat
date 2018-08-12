package org.training.chat.data;

/**
 * Данные, которые передаются при создании чата
 */
public class RequestCreateChat {
    private String loginReceiver;

    public RequestCreateChat() {
    }

    public RequestCreateChat(String loginReceiver) {
        this.loginReceiver = loginReceiver;
    }

    public String getLoginReceiver() {
        return loginReceiver;
    }

    public void setLoginReceiver(String loginReceiver) {
        this.loginReceiver = loginReceiver;
    }
}
