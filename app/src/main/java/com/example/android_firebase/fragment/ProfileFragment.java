package com.example.android_firebase.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.android_firebase.R;
import com.example.android_firebase.activity.MainActivity;
import com.example.android_firebase.activity.ManagerPostActivity;
import com.example.android_firebase.activity.SignInActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class ProfileFragment extends Fragment {
    private Button signOutButton, editProfile, managerPost;
    private FirebaseAuth auth;
    private ImageView avatarProfile;
    private TextView userNameProfile, emailProfile;
    private EditText editTextUserName;
    private ProgressBar progressBarUpdateProfile;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_fragment, container, false);
        init(view);
        listenEvent();
        loadProfileUser();
        return view;
    }

    void init(View view) {
        auth = FirebaseAuth.getInstance();
        signOutButton = view.findViewById(R.id.sign_out);
        avatarProfile = view.findViewById(R.id.userAvatarProfile);
        userNameProfile = view.findViewById(R.id.userNameProfile);
        emailProfile = view.findViewById(R.id.emailProfile);
        editTextUserName = view.findViewById(R.id.editTextUsername);
        editProfile = view.findViewById(R.id.editProfile);
        progressBarUpdateProfile = view.findViewById(R.id.progressBarUpdateProfile);
        managerPost = view.findViewById(R.id.managerPost);

        db = FirebaseFirestore.getInstance();
    }

    void loadProfileUser() {
        FirebaseUser user = auth.getCurrentUser();
        Glide.with(getContext()).load(user.getPhotoUrl()).into(avatarProfile);
        userNameProfile.setText(user.getDisplayName());
        emailProfile.setText(user.getEmail());
        editTextUserName.setText(user.getDisplayName());
    }

    boolean validateDataBeforeUpdate(String userName) {
        if(userName.trim().equals("")) {
            editTextUserName.setError("Name is required!");
            return false;
        }
        return true;
    }

    void handleEditProfile() {
        FirebaseUser user = auth.getCurrentUser();
        if(user.getDisplayName().equals(editTextUserName.getText().toString().trim()))
        {
            return;
        }
        String newUserName = editTextUserName.getText().toString().trim();
        if(!validateDataBeforeUpdate(newUserName)) return;
        isLoadingWhenUpdateProfile(true);
        DocumentReference documentReference = db.collection("users").document(user.getUid());
        UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                .setDisplayName(newUserName)
                .build();
        user.updateProfile(userProfileChangeRequest);
        documentReference.update("displayName", newUserName).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                userNameProfile.setText(newUserName);
                editTextUserName.setText(newUserName);
                isLoadingWhenUpdateProfile(false);
                Toast.makeText(getContext(), "Update username success ", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                isLoadingWhenUpdateProfile(false);
                Toast.makeText(getContext(), "Update username failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    void isLoadingWhenUpdateProfile(boolean isLoading) {
        if(isLoading) {
            progressBarUpdateProfile.setVisibility(View.VISIBLE);
            editProfile.setVisibility(View.GONE);
        } else {
            progressBarUpdateProfile.setVisibility(View.GONE);
            editProfile.setVisibility(View.VISIBLE);
        }
    }

    void listenEvent() {
        signOutButton.setOnClickListener(v -> signOut());
        editProfile.setOnClickListener(v -> handleEditProfile());
        managerPost.setOnClickListener(v -> startActivity(new Intent(getContext(), ManagerPostActivity.class)));
    }

    private void signOut() {
        Toast.makeText(getContext(), "Sign Out...", Toast.LENGTH_SHORT).show();
        DocumentReference documentReference = db.collection("users")
                .document(auth.getCurrentUser().getUid());
        HashMap<String, Object> updated = new HashMap<>();
        updated.put("token", FieldValue.delete());
        documentReference.update(updated).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                auth.signOut();
                Intent intent = new Intent(getActivity(), SignInActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Sign out failed" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

