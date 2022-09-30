package com.example.android_firebase.models;

public class Data {
    String senderName;
    String chatUserId;
    String text;

    public Data(String senderName, String chatUserId, String text) {
        this.senderName = senderName;
        this.chatUserId = chatUserId;
        this.text = text;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getChatUserId() {
        return chatUserId;
    }

    public void setChatUserId(String chatUserId) {
        this.chatUserId = chatUserId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
