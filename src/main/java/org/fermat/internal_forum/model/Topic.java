package org.fermat.internal_forum.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;
import org.fermat.CryptoBytes;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mati on 03/04/17.
 */
@Entity
public class Topic implements Serializable{

    static final long serialVersionUID = 42241553333L;

    @PrimaryKey
    private long id;
    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    private String ownerPk;
    @SecondaryKey(relate = Relationship.ONE_TO_ONE)
    private String title;
    private String subTitle;
    private List<String> category;
    private String raw;
    private int postCount;
    private List<Post> posts;
    private long ccValueInToshis;
    private long pubTime;
    private byte[] signature;
    private int topicVersion = 0;

    public Topic() {
    }

    public Topic(long id, String ownerPk, String title, String subTitle, String category, String raw, int commentCount, long ccValueInToshis, long pubTime, String signature) {
        this.id = id;
        this.ownerPk = ownerPk;
        this.title = title;
        this.subTitle = subTitle;
        this.category = new ArrayList<>();
        this.category.add(category);
        this.raw = raw;
        this.postCount = commentCount;
        this.ccValueInToshis = ccValueInToshis;
        this.pubTime = pubTime;
        this.signature = CryptoBytes.fromHexToBytes(signature);
    }

    public static Topic newTopic(String ownerPk, String title, String subTitle, List<String> category, String raw, byte[] signature, long ccValue){
        return new Topic(ownerPk,title,subTitle,category,raw,signature,ccValue);
    }

    public Topic(String ownerPk, String title, String subTitle, List<String> category, String raw, byte[] signature, long ccValue) {
        this.ownerPk = ownerPk;
        this.title = title;
        this.subTitle = subTitle;
        this.category = category;
        this.raw = raw;
        this.signature = signature;
        this.ccValueInToshis = ccValue;
    }

    public Topic(long id, String ownerPk, String title, String subTitle, List<String> category, String raw, List<Post> posts, long pubTime, byte[] signature) {
        this.id = id;
        this.ownerPk = ownerPk;
        this.title = title;
        this.subTitle = subTitle;
        this.category = category;
        this.raw = raw;
        this.posts = posts;
        this.pubTime = pubTime;
        this.signature = signature;
    }

    public long getId() {
        return id;
    }

    public String getOwnerPk() {
        return ownerPk;
    }

    public String getTitle() {
        return title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public List<String> getCategory() {
        return category;
    }

    public String getRaw() {
        return raw;
    }

    public List<Post> getPosts() {
        return posts;
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

    public void addPost(Post post) {
        if (this.posts==null){
            this.posts = new ArrayList<>();
        }
        this.posts.add(post);
    }

    public long getCcValueInToshis() {
        return ccValueInToshis;
    }

    public String toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id",id);
        jsonObject.addProperty("ownerPk",ownerPk);
        jsonObject.addProperty("title",title);
        jsonObject.addProperty("subTitle",subTitle);
        jsonObject.addProperty("category",category.get(0));
        jsonObject.addProperty("raw",raw);
        jsonObject.addProperty("comments_count",(posts!=null)?posts.size():0);
        jsonObject.addProperty("ccValueInToshis",ccValueInToshis);
        jsonObject.addProperty("pubTime",pubTime);
        jsonObject.addProperty("signature", CryptoBytes.toHexString(signature));
        return jsonObject.toString();
    }

    public static Topic fromJson(String s){
        JsonObject jsonObject = (JsonObject) new JsonParser().parse(s);
        long id = jsonObject.get("id").getAsLong();
        String ownerPk = jsonObject.get("ownerPk").getAsString();
        String title = jsonObject.get("title").getAsString();
        String subTitle = jsonObject.get("subTitle").getAsString();
        String category = jsonObject.get("category").getAsString();
        String raw = jsonObject.get("raw").getAsString();
        int commentCount = jsonObject.get("comments_count").getAsInt();
        long ccValueInToshis = jsonObject.get("ccValueInToshis").getAsLong();
        long pubTime = jsonObject.get("pubTime").getAsLong();
        String signature = jsonObject.get("signature").getAsString();
        return new Topic(id,ownerPk,title,subTitle,category,raw,commentCount,ccValueInToshis,pubTime,signature);
    }

//    public static Topic fromJson(String s){
//        return new Gson().fromJson(s,Topic.class);
//    }

    @Override
    public String toString() {
        return "Topic{" +
                "id=" + id +
                ", ownerPk='" + ownerPk + '\'' +
                ", title='" + title + '\'' +
                ", subTitle='" + subTitle + '\'' +
                ", category=" + category +
                ", raw='" + raw + '\'' +
                ", posts=" + posts +
                ", pubTime=" + pubTime +
                ", signature=" + Arrays.toString(signature) +
                '}';
    }

    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }
}
