package fr.insalyon.hermes.model;

import java.util.Date;


/**
 * Message used to exchange added notifications
 */
public class AddNotification extends Notification{
    private LogChat chat;

    public AddNotification(String content, String sender, String destination, Date time) {
        super(content,sender, destination, time,"add");
    }



    public void setChat(LogChat logChat) {
        this.chat = logChat;
    }

    public LogChat getChat() {
        return this.chat;
    }
}
