package org.fermat.push_notifications;

/**
 * Created by mati on 11/04/17.
 */
public enum SuscriptionType {

    TOPICS("tps"),TOPIC("tp");

    String id;

    SuscriptionType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
