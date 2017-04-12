package org.fermat.push_notifications.message;

/**
 * Created by mati on 11/04/17.
 */
public class BasePushMessage<T> {

    private int codeType;

    public BasePushMessage(int codeType) {
        this.codeType = codeType;
    }

    public String toJson(){
        return null;
    }

    public T fromJson(String s){
        return null;
    }
}
