package fr.insalyon.hermes.model;

import java.util.Date;
import java.util.List;

/**
 * Message used to add users in a chat
 */
public class AddUserChat extends Message {
    private String chatName;
    private List<String> userName;

    public AddUserChat(String sender, String destination, Date time, String chatName, List<String> userName) {
        super(sender, destination, time);
        this.chatName = chatName;
        this.userName = userName;
    }

    public String getChatName(){
        return this.chatName;
    }

    public List<String> getUsers(){
        return this.userName;
    }
}
