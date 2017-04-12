package org.fermat.internal_forum.model;

import com.google.gson.Gson;

import java.util.Arrays;

/**
 * Created by mati on 08/04/17.
 */

public class Comment {

    private long timestamp;
    private String ownerPk;
    private String ownerName;
    private byte[] ownerImg;
    private String text;

    public Comment(long timestamp, String ownerPk, String ownerName, byte[] ownerImg, String text) {
        this.timestamp = timestamp;
        this.ownerPk = ownerPk;
        this.ownerName = ownerName;
        this.ownerImg = ownerImg;
        this.text = text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getOwnerPk() {
        return ownerPk;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public byte[] getOwnerImg() {
        return ownerImg;
    }

    public String getText() {
        return text;
    }

    public String toJson(){
        return new Gson().toJson(this);
    }
    public Comment fromJson(String s){
        return new Gson().fromJson(s,Comment.class);
    }

    @Override
    public String toString() {
        return "Comment{" +
                "text='" + text + '\'' +
                ", ownerImg=" + Arrays.toString(ownerImg) +
                ", ownerName='" + ownerName + '\'' +
                ", ownerPk='" + ownerPk + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}