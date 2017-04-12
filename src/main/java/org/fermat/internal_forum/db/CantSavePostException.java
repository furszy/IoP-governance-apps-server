package org.fermat.internal_forum.db;

/**
 * Created by mati on 03/04/17.
 */
public class CantSavePostException extends Exception {

    public CantSavePostException(String message) {
        super(message);
    }

    public CantSavePostException(Throwable cause) {
        super(cause);
    }
}
