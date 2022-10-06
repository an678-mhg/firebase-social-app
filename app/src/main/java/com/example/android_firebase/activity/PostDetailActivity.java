package com.example.android_firebase.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.android_firebase.R;
import com.example.android_firebase.adapter.CommentAdapter;
import com.example.android_firebase.models.Comment;
import com.example.android_firebase.models.Post;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class PostDetailActivity extends AppCompatActivity {
    private ImageView imagePostContent, imageAvatarUser, backButton;
    private TextView postUserName, postUserSubName, postContentTitle, textViewNoComment, toolbarUserName;
    private ProgressBar progressBar, progressBarComment;
    private ScrollView scrollView;
    private FlexboxLayout boxUserInfo;
    private FlexboxLayout boxUserInfoLoading;
    private EditText editTextComment;
    private RecyclerView listComment;
    private CommentAdapter commentAdapter;
    private ArrayList<Comment> commentArrayList;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FrameLayout sendButton;
    private String postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        init();
        listenEvent();
        loadDataPostDetail();
        loadCommentFromFirebase();
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
        editTextComment = findViewById(R.id.editTextComment);
        progressBarComment = findViewById(R.id.progressBarComment);
        listComment = findViewById(R.id.listComment);
        textViewNoComment = findViewById(R.id.textViewNoComment);
        sendButton = findViewById(R.id.buttonSendMess);
        backButton = findViewById(R.id.backButton);
        toolbarUserName = findViewById(R.id.toolbarUserName);

        Bundle bundle = getIntent().getExtras();
        if(bundle == null) return;
        postId = (String) bundle.get("postId");
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        commentArrayList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentArrayList);
        linearLayoutManager = new LinearLayoutManager(this);
        listComment.setLayoutManager(linearLayoutManager);
    }

    private void listenEvent() {
        sendButton.setOnClickListener(v -> {
            postCommentFirebase();
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        if(inputMethodManager.isAcceptingText()){
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(),
                    0
            );
        }
    }

    private void postCommentFirebase() {
        if(editTextComment.getText().toString().trim().equals("")) return;
        addCommentInAdapter();
        HashMap<String, Object> comment = new HashMap<>();
        comment.put("postId", postId);
        comment.put("userId", auth.getCurrentUser().getUid());
        comment.put("text", editTextComment.getText().toString());
        comment.put("createdAt", Timestamp.now());
        db.collection("comments").add(comment);
        editTextComment.setText("");
        hideSoftKeyboard(this);
    }

    private void addCommentInAdapter() {
        commentArrayList.add(new Comment(postId, auth.getCurrentUser().getUid(), editTextComment.getText().toString(), Timestamp.now()));
        commentAdapter.notifyDataSetChanged();
    }

    private void loadCommentFromFirebase() {
        db.collection("comments").whereEqualTo("postId", postId).orderBy("createdAt", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    for(QueryDocumentSnapshot document : task.getResult()) {
                        String userId = document.getString("userId");
                        String postId = document.getString("postId");
                        String text = document.getString("text");
                        Timestamp createAt = document.getTimestamp("createdAt");
                        Comment comment = new Comment(postId, userId, text, createAt);
                        if(comment != null) {
                            commentArrayList.add(comment);
                        }
                    }
                    if(commentArrayList.size() > 0) {
                        commentAdapter.setCommentArrayList(commentArrayList);
                        listComment.setAdapter(commentAdapter);
                        progressBarComment.setVisibility(View.GONE);
                        listComment.setVisibility(View.VISIBLE);
                    } else {
                        progressBarComment.setVisibility(View.GONE);
                        listComment.setVisibility(View.VISIBLE);
                        textViewNoComment.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(PostDetailActivity.this, "get failed", Toast.LENGTH_SHORT).show();
                    progressBarComment.setVisibility(View.GONE);
                    listComment.setVisibility(View.VISIBLE);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBarComment.setVisibility(View.GONE);
                Log.e("Error", e.getMessage());
                Toast.makeText(PostDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDataPostDetail() {
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
                    toolbarUserName.setText(documentSnapshot.getString("displayName"));
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(commentAdapter != null) {
            commentAdapter.release();
        }
    }
}