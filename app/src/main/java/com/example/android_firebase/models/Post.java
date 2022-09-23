package com.example.android_firebase.models;

import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.util.ArrayList;

public class Post implements Serializable {
    private String id;
    private String title;
    private String imageUri;
    private String userId;
    private ArrayList<String> likes;
    private Timestamp createdAt;

    public Post(String id, String title, String imageUri, String userId, ArrayList<String> likes, Timestamp createdAt) {
        this.id = id;
        this.title = title;
        this.imageUri = imageUri;
        this.userId = userId;
        this.likes = likes;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public ArrayList<String> getLikes() {
        return likes;
    }

    public void setLikes(ArrayList<String> likes) {
        this.likes = likes;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
