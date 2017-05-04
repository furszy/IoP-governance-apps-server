package org.fermat.internal_forum.model;

import com.google.gson.Gson;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.PrimaryKey;
import javafx.geometry.Pos;

import java.io.Serializable;
import java.util.List;

/**
 * Created by mati on 03/04/17.
 */
@Persistent
public class Post implements Serializable{

    static final long serialVersionUID = 42241553233L;

    private long id;
    private long topicId;
    private String ownerPk;
    private String raw;
    private long pubTime;
    private byte[] signature;

    public Post() {
    }

    public static Post newPost(long topicId, String ownerPk, String raw, byte[] signature){
        return new Post(topicId,ownerPk,raw,signature);
    }

    public Post(long topicId, String ownerPk, String raw, byte[] signature) {
        this.topicId = topicId;
        this.ownerPk = ownerPk;
        this.raw = raw;
        this.signature = signature;
    }

    public Post(long id, long topicId, String ownerPk, String raw, long pubTime, byte[] signature) {
        this.id = id;
        this.topicId = topicId;
        this.ownerPk = ownerPk;
        this.raw = raw;
        this.pubTime = pubTime;
        this.signature = signature;
    }

    public long getId() {
        return id;
    }

    public long getTopicId() {
        return topicId;
    }

    public String getOwnerPk() {
        return ownerPk;
    }

    public String getRaw() {
        return raw;
    }

    public long getPubTime() {
        return pubTime;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setPubTime(long pubTime) {
        this.pubTime = pubTime;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static Post fromJson(String s){
        return new Gson().fromJson(s,Post.class);
    }
}
