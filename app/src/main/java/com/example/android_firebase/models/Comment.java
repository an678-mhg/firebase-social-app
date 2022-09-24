package com.example.android_firebase.models;

import com.google.firebase.Timestamp;

public class Comment {
    private String postId;
    private String userId;
    private String text;
    private Timestamp createdAt;

    public Comment(String postId, String userId, String text, Timestamp createdAt) {
        this.postId = postId;
        this.userId = userId;
        this.text = text;
        this.createdAt = createdAt;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
