package com.example.android_firebase.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.android_firebase.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class EditPostActivity extends AppCompatActivity {
    ImageView backButton;
    TextView textView;
    EditText editTextTitle;
    ProgressBar progressBar;
    Button buttonEdit;
    String postId;
    String postTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);
        init();
        listenEvent();
    }

    private void init() {
        backButton = findViewById(R.id.backButton);
        textView = findViewById(R.id.toolbarUserName);
        editTextTitle = findViewById(R.id.editTextTitle);
        progressBar = findViewById(R.id.progressBar);
        buttonEdit = findViewById(R.id.buttonEdit);
        Bundle bundle = getIntent().getExtras();
        postId = bundle.getString("postId");
        postTitle = bundle.getString("postTitle");
        editTextTitle.setText(postTitle);
    }

    private void listenEvent() {
        buttonEdit.setOnClickListener(v -> handleEditPost());
        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void handleEditPost() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if(!editTextTitle.getText().toString().trim().equals("")) {
            if(postId != null) {
                isLoading(true);
                db.collection("posts").document(postId).update("title", editTextTitle.getText().toString().trim())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                isLoading(false);
                                startActivity(new Intent(EditPostActivity.this, ManagerPostActivity.class));
                                Toast.makeText(EditPostActivity.this, "Edit post success", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(EditPostActivity.this, "Edit post failed" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                isLoading(false);
                            }
                        });
            }
        }
    }

    private void isLoading(boolean loading) {
        if(loading) {
            buttonEdit.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            buttonEdit.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }
}