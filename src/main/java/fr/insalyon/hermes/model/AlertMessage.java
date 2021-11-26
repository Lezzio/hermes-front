package fr.insalyon.hermes.model;

import java.util.Date;

/**
 * Allows to exchange an alert message
 */
public class AlertMessage extends Message {

    private final String content;

    public AlertMessage(String content, String sender, String destination, Date time) {
        super(sender, destination, time);
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
