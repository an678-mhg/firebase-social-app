package com.example.android_firebase.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.android_firebase.R;
import com.example.android_firebase.models.Post;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;

public class PostDetailActivity extends AppCompatActivity {
    private ImageView imagePostContent, imageAvatarUser;
    private TextView postUserName, postUserSubName, postContentTitle;
    private ProgressBar progressBar;
    private ScrollView scrollView;
    private FlexboxLayout boxUserInfo;
    private FlexboxLayout boxUserInfoLoading;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        init();
        loadDataPostDetail();
    }

    private void init() {
        imageAvatarUser = findViewById(R.id.postDetailAvatar);
        imagePostContent = findViewById(R.id.detailImage);
        postContentTitle = findViewById(R.id.postDetailTitle);
        postUserName = findViewById(R.id.postDetailUserName);
        postUserSubName = findViewById(R.id.postDetailSubUserName);
        progressBar = findViewById(R.id.progressBar);
        scrollView = findViewById(R.id.postDetailLayout);
        boxUserInfo = findViewById(R.id.box_info_user);
        boxUserInfoLoading = findViewById(R.id.box_info_user_loading);
        db = FirebaseFirestore.getInstance();
    }

    private void loadDataPostDetail() {
        Bundle bundle = getIntent().getExtras();
        if(bundle == null) return;
        String postId = (String) bundle.get("postId");
        db.collection("posts").document(postId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()) {
                    postUserSubName.setText(convertTimeStamp(documentSnapshot.getTimestamp("createAt")));
                    postContentTitle.setText(documentSnapshot.getString("title"));
                    Glide.with(PostDetailActivity.this).load(documentSnapshot.getString("imageUri")).into(imagePostContent);
                    getUserInfo(documentSnapshot.getString("userId"));
                    progressBar.setVisibility(View.GONE);
                    scrollView.setVisibility(View.VISIBLE);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PostDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getUserInfo(String userId) {
        db.collection("users").document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()) {
                    Glide.with(PostDetailActivity.this).load(documentSnapshot.getString("photoURL")).into(imageAvatarUser);
                    postUserName.setText(documentSnapshot.getString("displayName"));
                    boxUserInfoLoading.setVisibility(View.GONE);
                    boxUserInfo.setVisibility(View.VISIBLE);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PostDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                boxUserInfoLoading.setVisibility(View.GONE);
                boxUserInfo.setVisibility(View.VISIBLE);
            }
        });
    }

    private String convertTimeStamp(Timestamp timestamp) {
        SimpleDateFormat sfd = new SimpleDateFormat("dd/MM/yyyy");
        return sfd.format(timestamp.toDate());
    }
}