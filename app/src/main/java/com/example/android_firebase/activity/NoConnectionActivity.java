package com.example.android_firebase.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.android_firebase.R;

public class NoConnectionActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private Button buttonTryAgain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_connection);

        progressBar = findViewById(R.id.progressBar);
        buttonTryAgain = findViewById(R.id.buttonTryAgain);

        buttonTryAgain.setOnClickListener(v -> handleTryAgain());
    }

    private void handleTryAgain() {
        progressBar.setVisibility(View.VISIBLE);
        buttonTryAgain.setVisibility(View.GONE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(isNetworkConnected()) {
                    startActivity(new Intent(NoConnectionActivity.this, MainActivity.class));
                } else {
                    Toast.makeText(NoConnectionActivity.this, "No connection!", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
                buttonTryAgain.setVisibility(View.VISIBLE);
            }
        }, 1000);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}