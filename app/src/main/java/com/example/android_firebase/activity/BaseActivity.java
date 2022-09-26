package com.example.android_firebase.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class BaseActivity extends AppCompatActivity {

    private DocumentReference documentReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        documentReference = db.collection("users")
                .document(auth.getCurrentUser().getUid());
    }

    @Override
    protected void onPause() {
        super.onPause();
        documentReference.update("isOnline", 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        documentReference.update("isOnline", 1);
    }
}
