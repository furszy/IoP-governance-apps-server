package org.fermat.db;

import java.io.Serializable;

/**
 * Created by mati on 16/12/16.
 */
public class IdentityData implements Serializable {


    private String name;
    private String password;
    private String email;

    private String apiKey;

    public IdentityData(String name, String password, String email,String apiKey) {
        this.name = name;
        this.password = password;
        this.email = email;
        this.apiKey = apiKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
