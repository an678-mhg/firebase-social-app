package com.example.android_firebase.models;

import java.util.ArrayList;

public class FCMResponse {
    String multicast_id;
    int success;
    int failure;
    int canonical_ids;
    ArrayList<ResultFCM> results;

    public FCMResponse(String multicast_id, int success, int failure, int canonical_ids, ArrayList<ResultFCM> results) {
        this.multicast_id = multicast_id;
        this.success = success;
        this.failure = failure;
        this.canonical_ids = canonical_ids;
        this.results = results;
    }

    public String getMulticast_id() {
        return multicast_id;
    }

    public void setMulticast_id(String multicast_id) {
        this.multicast_id = multicast_id;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public int getFailure() {
        return failure;
    }

    public void setFailure(int failure) {
        this.failure = failure;
    }

    public int getCanonical_ids() {
        return canonical_ids;
    }

    public void setCanonical_ids(int canonical_ids) {
        this.canonical_ids = canonical_ids;
    }

    public ArrayList<ResultFCM> getResults() {
        return results;
    }

    public void setResults(ArrayList<ResultFCM> results) {
        this.results = results;
    }
}
