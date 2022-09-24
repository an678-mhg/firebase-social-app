package com.example.android_firebase.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.android_firebase.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ForgotPassword extends AppCompatActivity {
    private EditText emailEditText;
    private Button passwordRetrieval;
    private ProgressBar progressBar;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        init();
        listenEvent();
    }

    void init() {
        emailEditText = findViewById(R.id.editTextEmail);
        passwordRetrieval = findViewById(R.id.passwordRetrieval);
        progressBar = findViewById(R.id.progressBar);
        auth = FirebaseAuth.getInstance();
    }

    void listenEvent() {
        passwordRetrieval.setOnClickListener(v -> handlePasswordRetrieval(emailEditText.getText().toString()));
    }

    void handlePasswordRetrieval(String email) {
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Email is invalid!");
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        passwordRetrieval.setVisibility(View.GONE);
        auth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(ForgotPassword.this, "Please check your email to change your password!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ForgotPassword.this, SignInActivity.class));
                progressBar.setVisibility(View.GONE);
                passwordRetrieval.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ForgotPassword.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                passwordRetrieval.setVisibility(View.VISIBLE);
            }
        });
    }
}