package com.example.android_firebase.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.android_firebase.R;
import com.example.android_firebase.models.Post;

public class PostDetailActivity extends AppCompatActivity {
    private ImageView imagePostContent, imageAvatarUser;
    private TextView postUserName, postUserSubName, postContentTitle;

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
    }

    private void loadDataPostDetail() {
        Bundle bundle = getIntent().getExtras();
        if(bundle == null) return;

        String postId = (String) bundle.get("postId");
        Toast.makeText(this, postId, Toast.LENGTH_SHORT).show();
    }
}