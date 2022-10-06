package com.example.android_firebase.models;

import com.google.firebase.Timestamp;

import java.util.ArrayList;

public class Conventions {
    String id;
    ArrayList<String> members;
    Timestamp createAt;
    String lastMessage;

    public Conventions(String id, ArrayList<String> members, Timestamp createAt, String lastMessage) {
        this.id = id;
        this.members = members;
        this.createAt = createAt;
        this.lastMessage = lastMessage;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<String> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<String> members) {
        this.members = members;
    }

    public Timestamp getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Timestamp createAt) {
        this.createAt = createAt;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
}
