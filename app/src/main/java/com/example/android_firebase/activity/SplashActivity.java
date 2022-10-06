package com.example.android_firebase.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;

import com.example.android_firebase.R;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(isNetworkConnected()) {
                    auth = FirebaseAuth.getInstance();
                    if(auth.getCurrentUser() == null) {
                        // Log out
                        Intent intent = new Intent(SplashActivity.this, SignInActivity.class);
                        startActivity(intent);
                    } else {
                        // Sign In
                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                } else {
                    Intent intent = new Intent(SplashActivity.this, NoConnectionActivity.class);
                    startActivity(intent);
                }
                finish();
            }
        }, 1000);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}