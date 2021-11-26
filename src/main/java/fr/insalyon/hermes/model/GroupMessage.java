package fr.insalyon.hermes.model;

import java.util.Date;

/**
 * Message used to send group message
 */
public class GroupMessage extends Message {

    private final String content;

    public GroupMessage(String content, String sender, String destination, Date time) {
        super(sender, destination, time);
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}