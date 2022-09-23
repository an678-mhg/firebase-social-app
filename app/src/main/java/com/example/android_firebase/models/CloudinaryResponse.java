package com.example.android_firebase.models;

public class CloudinaryResponse {
    String url;

    public CloudinaryResponse(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
