package com.example.android_firebase.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toolbar;

import com.example.android_firebase.R;
import com.example.android_firebase.fragment.ChatFragment;
import com.example.android_firebase.fragment.HomeFragment;
import com.example.android_firebase.fragment.ProfileFragment;
import com.example.android_firebase.fragment.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;


public class MainActivity extends BaseActivity {
    BottomNavigationView bottomNavigationView;
    HomeFragment homeFragment = new HomeFragment();
    SearchFragment searchFragment = new SearchFragment();
    ChatFragment chatFragment = new ChatFragment();
    ProfileFragment profileFragment = new ProfileFragment();
    FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handleUseBottomNavigation();
        init();
        listenEvent();
    }

    void handleUseBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_nav);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout, homeFragment).commit();
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home_menu:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout, homeFragment).commit();
                        return true;
                    case R.id.search_menu:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout, searchFragment).commit();
                        return true;
                    case R.id.chat_menu:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout, chatFragment).commit();
                        return true;
                    case R.id.profile_menu:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout, profileFragment).commit();
                        return true;
                }
                return false;
            }
        });
    }

    void init() {
        floatingActionButton = findViewById(R.id.button_add_post);
    }

    void listenEvent() {
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddPostActivity.class);
                startActivity(intent);
            }
        });
    }
}