package org.fermat.internal_forum.db;

/**
 * Created by mati on 12/04/17.
 */
public class CantUpdateProfileException extends Exception {
    public CantUpdateProfileException(Exception e) {
        super(e);
    }
}
