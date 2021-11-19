package fr.insalyon.hermes.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GetChats extends Message {
    private List<ChatInfo> logChats;

    public GetChats(String sender, String destination, Date time) {
        super(sender, destination, time);
        this.logChats = new ArrayList<ChatInfo>();
    }

    public void add(ChatInfo chat){
        logChats.add(chat);
    }

    public void setLogChat(List<ChatInfo> logchats) {
        this.logChats = logchats;
    }

    public List<ChatInfo> getChats() {
        return logChats;
    }
}
