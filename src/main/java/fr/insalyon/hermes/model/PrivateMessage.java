package fr.insalyon.hermes.model;

import java.util.Date;

public class PrivateMessage extends Message {

    private final String content;

    public PrivateMessage(String content, String sender, String destination, Date time) {
        super(sender, destination, time);
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}