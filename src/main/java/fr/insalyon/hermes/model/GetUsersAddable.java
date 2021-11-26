package fr.insalyon.hermes.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Message used to get users that are addable in a chat
 */
public class GetUsersAddable extends Message {
    private List<String> users;

    public GetUsersAddable(String sender, String destination, Date time) {
        super(sender, destination, time);
        this.users = new ArrayList<String>();
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public List<String> getUsers() {
        return this.users;
    }
}
