package fr.insalyon.hermes.model;

import java.util.Date;

/**
 * Allows to inform the server of a connection
 */
public class ConnectionMessage extends Message {
    private String password;
    private String userName;


    public ConnectionMessage(String sender, String password, Date time) {
        super(sender, "server", time);
        this.password = password;
        this.userName = sender;
    }

    public String getName(){
        return this.userName;
    }
}
