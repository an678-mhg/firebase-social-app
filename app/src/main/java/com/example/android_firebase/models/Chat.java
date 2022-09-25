package com.example.android_firebase.models;

import com.google.firebase.Timestamp;

public class Chat {
    private String roomId;
    private String text;
    private String senderId;
    private Timestamp createAt;

    public Chat(String roomId, String text, String senderId, Timestamp createAt) {
        this.roomId = roomId;
        this.text = text;
        this.senderId = senderId;
        this.createAt = createAt;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public Timestamp getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Timestamp createAt) {
        this.createAt = createAt;
    }
}
