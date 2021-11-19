package fr.insalyon.hermes.model;

import java.util.Date;

public class MultiCastMessage{

    private final String content;
    private final String sender;
    private final Date time;

    public MultiCastMessage(String content, String sender, Date time) {
        this.content = content;
        this.sender = sender;
        this.time = time;
    }

    public String getContent() {
        return content;
    }

}
