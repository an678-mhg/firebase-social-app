package com.example.android_firebase.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.android_firebase.R;
import com.example.android_firebase.activity.MainActivity;
import com.example.android_firebase.activity.SignInActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class ProfileFragment extends Fragment {
    Button signOutButton;
    FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_fragment, container, false);
        auth = FirebaseAuth.getInstance();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        listenEvent();
    }

    void init(View view) {
        auth = FirebaseAuth.getInstance();
        signOutButton = view.findViewById(R.id.sign_out);
    }

    void listenEvent() {
        signOutButton.setOnClickListener(v -> signOut());
    }

    private void signOut() {
        Toast.makeText(getContext(), "Sign Out...", Toast.LENGTH_SHORT).show();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference = db.collection("users")
                .document(auth.getCurrentUser().getUid());
        HashMap<String, Object> updated = new HashMap<>();
        updated.put("token", FieldValue.delete());
        documentReference.update(updated).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
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

