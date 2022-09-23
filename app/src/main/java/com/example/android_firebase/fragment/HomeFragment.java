package com.example.android_firebase.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

import java.util.ArrayList;
import java.util.Collections;

public class HomeFragment extends Fragment {
    private RecyclerView listPost;
    private PostAdapter postAdapter;
    private ProgressBar progressBar;
    private ArrayList<Post> postArrayList;
    private TextView textNoPostRecently;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment, container, false);
        init(view);
        listenEvent();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        postArrayList = new ArrayList<>();
        postAdapter = new PostAdapter(postArrayList, firebaseAuth.getCurrentUser().getUid(), container.getContext());
        LinearLayoutManager linearLayout = new LinearLayoutManager(container.getContext());
        listPost.setLayoutManager(linearLayout);
        getPostsFromFireBase();
        return view;
    }

    public void init(View view) {
        listPost = view.findViewById(R.id.listPost);
        progressBar = view.findViewById(R.id.progressBar);
        textNoPostRecently = view.findViewById(R.id.text_no_post_recently);
    }

    public void listenEvent() {

    }

    public void getPostsFromFireBase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        progressBar.setVisibility(View.VISIBLE);
        db.collection("posts").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String id = document.getId();
                        String userId = document.getString("userId");
                        String title = document.getString("title");
                        String imageUri = document.getString("imageUri");
                        ArrayList<String> likes = (ArrayList<String>) document.get("likes");
                        Timestamp createdAt = document.getTimestamp("createAt");
                        Post post = new Post(id, title, imageUri, userId, likes, createdAt);
                        if(post != null) {
                            postArrayList.add(post);
                        }
                    }
                    if(postArrayList.size() > 0) {
                        postAdapter.setPosts(postArrayList);
                        listPost.setAdapter(postAdapter);
                        progressBar.setVisibility(View.GONE);
                        listPost.setVisibility(View.VISIBLE);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        listPost.setVisibility(View.GONE);
                        textNoPostRecently.setVisibility(View.VISIBLE);
                    }
                } else {

                }
            }
        });
    }
}
