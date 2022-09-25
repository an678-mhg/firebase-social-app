package com.example.android_firebase.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_firebase.R;
import com.example.android_firebase.adapter.UserAdapter;
import com.example.android_firebase.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class SearchFragment extends Fragment {
    private LinearLayout layoutSearch;
    private RecyclerView listUser;
    private ProgressBar progressBar;
    private UserAdapter userAdapter;
    private LinearLayoutManager linearLayoutManager;
    private ArrayList<User> userArrayList;
    private FirebaseFirestore db;
    private TextView textViewNoUsers;
    private EditText editTextSearch;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_fragment, container, false);
        init(view);
        listenEvent();
        loadUserFromFirebase();
        return view;
    }

    private void init(View view) {
        layoutSearch = view.findViewById(R.id.layoutSearch);
        listUser = view.findViewById(R.id.listUser);
        progressBar = view.findViewById(R.id.progressBar);
        textViewNoUsers = view.findViewById(R.id.textViewNoUsers);
        editTextSearch = view.findViewById(R.id.editTextSearch);

        userArrayList = new ArrayList<>();
        userAdapter = new UserAdapter(view.getContext(), userArrayList);
        linearLayoutManager = new LinearLayoutManager(view.getContext());
        listUser.setLayoutManager(linearLayoutManager);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private void listenEvent() {
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                userAdapter.getFilter().filter(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void loadUserFromFirebase() {
        db.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    for(QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        if(auth.getCurrentUser().getUid().equals(documentSnapshot.getId())) {
                            continue;
                        }
                        String id = documentSnapshot.getId();
                        String displayName = documentSnapshot.getString("displayName");
                        String photoURL = documentSnapshot.getString("photoURL");
                        String email = documentSnapshot.getString("email");
                        User user = new User(id, photoURL, displayName, email);
                        if(user != null) {
                            userArrayList.add(user);
                        }
                    }
                    if(userArrayList.size() > 0) {
                        userAdapter.setUserArrayList(userArrayList);
                        listUser.setAdapter(userAdapter);
                        progressBar.setVisibility(View.GONE);
                        listUser.setVisibility(View.VISIBLE);
                        layoutSearch.setVisibility(View.VISIBLE);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        listUser.setVisibility(View.GONE);
                        textViewNoUsers.setVisibility(View.VISIBLE);
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getView().getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(userAdapter != null) {
            userAdapter.release();
        }
    }
}
