package com.openweb4.model;

public class TweetAuthor {
    private String name;
    private String handle;
    private String profileUrl;

    public TweetAuthor() {
    }

    public TweetAuthor(String name, String handle, String profileUrl) {
        this.name = name;
        this.handle = handle;
        this.profileUrl = profileUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }
}
