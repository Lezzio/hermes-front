package fr.insalyon.hermes.model;

import java.util.List;


/**
 * Allows to create a chat info class
 */
public class ChatInfo {

    private String name;
    private List<String> users;
    private TextMessage lastMessage;
    private int usersNumber;
    private String admin;

    public ChatInfo(String name, List<String> users, TextMessage message) {
        this.name = name;
        this.users = users;
        this.lastMessage = message;
        this.usersNumber = users.size();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public TextMessage getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(TextMessage lastMessage) {
        this.lastMessage = lastMessage;
    }

    public int getUsersNumber() {
        return usersNumber;
    }

    public void setUsersNumber(int usersNumber) {
        this.usersNumber = usersNumber;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }
}