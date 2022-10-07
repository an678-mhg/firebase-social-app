package com.example.android_firebase.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_firebase.R;
import com.example.android_firebase.adapter.PostAdapter;
import com.example.android_firebase.models.Post;
import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ManagerPostActivity extends AppCompatActivity {
    private ImageView backButton;
    private TextView textView;
    private ArrayList<Post> arrayListPost;
    private PostAdapter postAdapter;
    private RecyclerView myListPost;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_post);
        init();
        listenEvent();
        getMyPost();
    }

    private void init() {
        backButton = findViewById(R.id.backButton);
        textView = findViewById(R.id.toolbarUserName);
        textView.setText("Manager Post");
        myListPost = findViewById(R.id.myListPost);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        arrayListPost = new ArrayList<>();
        postAdapter = new PostAdapter(arrayListPost, auth.getCurrentUser().getUid(), this);
        linearLayoutManager = new LinearLayoutManager(this);
        myListPost.setLayoutManager(linearLayoutManager);
        myListPost.setAdapter(postAdapter);
    }

    private void listenEvent() {
        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void getMyPost() {
        db.collection("posts").whereEqualTo("userId", auth.getCurrentUser().getUid()).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for(QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            String title = documentSnapshot.getString("title");
                            String userId = documentSnapshot.getString("userId");
                            String imageUri = documentSnapshot.getString("imageUri");
                            ArrayList<String> likes = (ArrayList<String>) documentSnapshot.get("likes");
                            Timestamp createAt = documentSnapshot.getTimestamp("createAt");
                            String id = documentSnapshot.getId();
                            Post post = new Post(id, title, imageUri, userId, likes, createAt);
                            if(post != null) {
                                arrayListPost.add(post);
                            }
                        }
                        if(arrayListPost.size() > 0) {
                            postAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        postAdapter.release();
    }
}