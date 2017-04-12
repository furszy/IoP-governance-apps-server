package org.fermat.internal_forum.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by mati on 03/04/17.
 */
public class Profile implements Serializable{

    static final long serialVersionUID = 42241511233L;

    private String pk;
    private String name;
    private byte[] img;

    private Set<String> pushDeviceIds;

    public Profile(String pk,String name) {
        this.pk = pk;
        this.name=name;
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

    public void addDeviceId(String deviceId){
        if (pushDeviceIds==null){
            pushDeviceIds = new HashSet<>();
        }
        pushDeviceIds.add(deviceId);
    }
}
