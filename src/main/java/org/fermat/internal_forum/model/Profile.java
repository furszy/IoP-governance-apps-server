package org.fermat.internal_forum.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;
import org.fermat.internal_forum.endpoints.base.InternalMsgProtocol;
import org.fermat.push_notifications.Firebase;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import static org.fermat.internal_forum.endpoints.base.InternalMsgProtocol.KEY_PROFILE_NAME;
import static org.fermat.internal_forum.endpoints.base.InternalMsgProtocol.KEY_PUBLIC_KEY;

/**
 * Created by mati on 03/04/17.
 */
@Entity
public class Profile implements Serializable{

    static final long serialVersionUID = 42241511233L;
    @PrimaryKey
    private String pk;
    @SecondaryKey(relate = Relationship.ONE_TO_ONE)
    private String name;
    private byte[] img;

    private Firebase.Type appType;

    private Set<String> pushDeviceIds;

    public Profile() {
    }

    public Profile(String pk, String name, Firebase.Type appType) {
        this.pk = pk;
        this.name=name;
        this.appType = appType;
    }

    public String getPk() {
        return pk;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImg(byte[] img) {
        this.img = img;
    }

    public void addDeviceId(String deviceId){
        if (pushDeviceIds==null){
            pushDeviceIds = new HashSet<>();
        }
        pushDeviceIds.add(deviceId);
    }

    public Firebase.Type getAppType() {
        return appType;
    }

    public String toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(KEY_PUBLIC_KEY,pk);
        jsonObject.addProperty(KEY_PROFILE_NAME,name);
        return jsonObject.toString();
    }

    public static Profile fromString(String json){
        String pk = null;
        String name = null;
        JsonObject jsonObject = (JsonObject) new JsonParser().parse(json);
        JsonElement jsonKey = jsonObject.get(KEY_PUBLIC_KEY);
        if (jsonKey!=null){
            pk = jsonKey.getAsString();
        }
        JsonElement jsonName = jsonObject.get(KEY_PROFILE_NAME);
        if (jsonName!=null){
            name = jsonName.getAsString();
        }
        return new Profile(pk,name,null);
    }
}
