package fr.insalyon.hermes.model;

import java.util.List;
import java.util.Objects;

public class LogChat {
    private String name;
    private List<String> users;
    private TextMessage message;
    private int usersNumber;

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public TextMessage getMessage() {
        return message;
    }

    public void setMessage(TextMessage message) {
        this.message = message;
    }

    public int getUsersNumber() {
        return usersNumber;
    }

    public void setUsersNumber(int usersNumber) {
        this.usersNumber = usersNumber;
    }

    public LogChat(String name, List<String> users, TextMessage message) {
        this.name = name;
        this.users = users;
        this.message = message;
        this.usersNumber = users.size();
    }

    public String getName() {
        return this.name;
    }

    public List<String> getUsers() {
        return this.users;
    }

    public void setTextMessage(TextMessage textMessage) {
        this.message = message;
    }

    public void setName(String chatName) {
        this.name = chatName;
    }


    public void removeUser(String user) {
        usersNumber--;
        users.remove(user);
    }

    public void addUser(String user){
        usersNumber++;
        users.add(user);
    }

    public String getOtherName(String name) {
        for(String user : users){
            if(!Objects.equals(user, name)){
                return user;
            }
        }
        return "";
    }
}
