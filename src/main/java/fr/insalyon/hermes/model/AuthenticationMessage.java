package fr.insalyon.hermes.model;

import java.util.Date;

/**
 * Allows a client to authenticate into the server
 */
public class AuthenticationMessage extends Message {

    private final String password;

    public AuthenticationMessage(String username, String password, Date time) {
        super(username, "server", time);
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

}