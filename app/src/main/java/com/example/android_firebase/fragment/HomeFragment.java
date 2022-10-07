package com.example.android_firebase.fragment;

import android.os.Bundle;

import android.util.Log;
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
import com.example.android_firebase.utils.Pagination;
import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    private RecyclerView listPost;
    private PostAdapter postAdapter;
    private ProgressBar progressBar, progressBarLoadMore;
    private ArrayList<Post> postArrayList;
    private TextView textNoPostRecently;
    private LinearLayoutManager linearLayout;
    private int limit = 5;
    private DocumentSnapshot lastDoc;
    private FirebaseFirestore db;
    private boolean isLoading;
    private boolean isLoadMore = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment, container, false);
        init(view);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        postArrayList = new ArrayList<>();
        postAdapter = new PostAdapter(postArrayList, firebaseAuth.getCurrentUser().getUid(), container.getContext());
        linearLayout = new LinearLayoutManager(container.getContext());
        listPost.setLayoutManager(linearLayout);
        progressBar.setVisibility(View.VISIBLE);
        listenEvent();
        getPostsFromFireBase();
        return view;
    }

    public void init(View view) {
        listPost = view.findViewById(R.id.listPost);
        progressBar = view.findViewById(R.id.progressBar);
        textNoPostRecently = view.findViewById(R.id.text_no_post_recently);
        progressBarLoadMore = view.findViewById(R.id.progressBarLoadMore);
        db = FirebaseFirestore.getInstance();
    }

    public void listenEvent() {
        listPost.addOnScrollListener(new Pagination(linearLayout) {
            @Override
            public void loadMoreItem() {
                if(lastDoc == null) return;
                isLoading = true;
                getNextData();
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }

            @Override
            public boolean isLoadMore() {
                return isLoadMore;
            }
        });
    }

    public void getNextData() {
        if(lastDoc != null) {
            progressBarLoadMore.setVisibility(View.VISIBLE);
            db.collection("posts").limit(limit).orderBy("createAt", Query.Direction.DESCENDING).startAfter(lastDoc).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()) {
                        if(task.getResult() != null) {
                            if(task.getResult().getDocuments().size() > 0) {
                                lastDoc = task.getResult().getDocuments().get(task.getResult().size() - 1);
                            } else {
                                isLoadMore = false;
                            }
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
                            postAdapter.notifyDataSetChanged();
                            progressBarLoadMore.setVisibility(View.GONE);
                        } else {
                            isLoadMore = false;
                        }
                    }
                    isLoading = false;
                }
            });
        }
    }

    public void getPostsFromFireBase() {
        isLoadMore = true;
        db.collection("posts").limit(limit).orderBy("createAt", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.getResult().getDocuments().get(task.getResult().size() - 1) != null) {
                    lastDoc = task.getResult().getDocuments().get(task.getResult().size() - 1);
                }
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
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(postAdapter != null) {
            postAdapter.release();
        }
    }
}
