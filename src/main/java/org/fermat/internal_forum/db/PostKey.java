package org.fermat.internal_forum.db;

import java.io.Serializable;

/**
 * Created by mati on 16/12/16.
 */
public class PostKey implements Serializable {

    private long id;

    public PostKey(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
