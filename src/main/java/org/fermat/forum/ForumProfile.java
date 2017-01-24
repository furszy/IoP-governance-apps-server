package org.fermat.forum;

import java.util.Map;

/**
 * Created by mati on 23/11/16.
 */

public class ForumProfile {

    private long forumId;
    private String name;
    private String username;
    private String password;
    private String email;
    private boolean isActive;

    public ForumProfile(long forumId, String username,boolean isActive) {
        this.forumId = forumId;
        this.username = username;
        this.isActive = isActive;
    }

    public ForumProfile(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public ForumProfile(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public ForumProfile(long id, String name, String username, boolean active) {
        this.forumId = id;
        this.name = name;
        this.username = username;
        this.isActive = active;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public long getForumId() {
        return forumId;
    }

    public String getName() {
        return name;
    }

    public void setForumId(long forumId) {
        this.forumId = forumId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }


}
