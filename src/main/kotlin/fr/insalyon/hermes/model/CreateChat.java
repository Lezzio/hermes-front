package fr.insalyon.hermes.model;

import java.util.Date;

public class CreateChat extends Message{

    private String chatName;

    public CreateChat(String sender, String destination, Date time, String chatName) {
        super(sender, destination, time);
        this.chatName = chatName;
    }

    public String getName() {
        return this.chatName;
    }
}
