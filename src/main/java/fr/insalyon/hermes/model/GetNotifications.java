package fr.insalyon.hermes.model;

import java.util.Date;
import java.util.List;

/**
 * Message used to get the list of notifications of a user
 */
public class GetNotifications extends Message {
    private List<Notification> notifications;

    public GetNotifications(String sender, String destination, Date time) {
        super(sender, destination, time);
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }

    public List<Notification> getNotifications() {
        return this.notifications;
    }
}
