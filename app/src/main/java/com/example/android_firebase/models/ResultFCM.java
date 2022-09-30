package com.example.android_firebase.models;

public class ResultFCM {
    String message_id;

    public ResultFCM(String message_id) {
        this.message_id = message_id;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }
}
