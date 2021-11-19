package fr.insalyon.hermes.model;

import java.util.List;

public class Chat {
    private String admin;
    private String chatName;
    private int users;
    private List<TextMessage> messages;

    public Chat(String chatName, int users, List<TextMessage> messages, String admin) {
        this.chatName = chatName;
        this.users = users;
        this.messages = messages;
        this.admin = admin;
    }

    public void add(TextMessage message) {
        messages.add(message);
    }

    public String getChatName() {
        return chatName;
    }

    public int getUsers() {
        return users;
    }

    public List<TextMessage> getMessages() {
        return messages;
    }

    public String getAdmin() {
        return admin;
    }
}
