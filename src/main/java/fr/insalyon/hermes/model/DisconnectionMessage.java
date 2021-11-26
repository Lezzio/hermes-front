package fr.insalyon.hermes.model;

import java.util.Date;

/**
 * Allows to inform the server of a disconnection
 */
public class DisconnectionMessage extends Message {



    public DisconnectionMessage(String sender,  Date time) {
        super(sender, "server",time);

    }
}
