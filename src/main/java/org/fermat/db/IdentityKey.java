package org.fermat.db;

import java.io.Serializable;

/**
 * Created by mati on 16/12/16.
 */
public class IdentityKey implements Serializable {

    private String user;

    public IdentityKey(String user) {
        this.user = user;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
