package com.example.android_firebase.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_firebase.R;
import com.example.android_firebase.adapter.ConventionAdapter;
import com.example.android_firebase.models.Conventions;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ChatFragment extends Fragment {
    private RecyclerView listConvention;
    private ProgressBar progressBar;
    private ConventionAdapter conventionAdapter;
    private TextView textNoMessage;
    private ArrayList<Conventions> conventionsArrayList;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chat_fragment, container, false);
        init(view);
        loadConventions();
        return view;
    }

    private void init(View view) {
        listConvention = view.findViewById(R.id.listConvention);
        progressBar = view.findViewById(R.id.progressBar);
        textNoMessage = view.findViewById(R.id.no_recent_messages);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        conventionsArrayList = new ArrayList<>();
        linearLayoutManager = new LinearLayoutManager(view.getContext());
        conventionAdapter = new ConventionAdapter(view.getContext(), conventionsArrayList, auth.getCurrentUser().getUid());
        listConvention.setLayoutManager(linearLayoutManager);
    }

    private void loadConventions() {
        db.collection("rooms").whereArrayContains("members", auth.getCurrentUser().getUid())
                .addSnapshotListener(((value, error) -> {
                    if(error != null) {
                        Toast.makeText(getContext(), "Load convention failed", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(value != null) {
                        for(DocumentChange doc : value.getDocumentChanges()) {
                            if(doc.getType() == DocumentChange.Type.ADDED) {
                                String lastMessage = doc.getDocument().getString("lastMessage");
                                Timestamp createAt = doc.getDocument().getTimestamp("createAt");
                                ArrayList<String> members = (ArrayList<String>) doc.getDocument().get("members");
                                String id = doc.getDocument().getId();
                                Conventions conventions = new Conventions(id, members, createAt, lastMessage);

                                if(conventions != null) {
                                    conventionsArrayList.add(conventions);
                                }
                            }
                            if(doc.getType() == DocumentChange.Type.MODIFIED) {
                                String lastMessage = doc.getDocument().getString("lastMessage");
                                if(lastMessage != null) {
                                    for(Conventions convention : conventionsArrayList) {
                                        if(doc.getDocument().getId().equals(convention.getId())) {
                                            convention.setLastMessage(lastMessage);
                                            conventionAdapter.notifyDataSetChanged();
                                        }
                                    }
                                }
                            }
                        }
                        if(conventionsArrayList.size() > 0) {
                            conventionAdapter.setConventionsArrayList(conventionsArrayList);
                            listConvention.setAdapter(conventionAdapter);
                            progressBar.setVisibility(View.GONE);
                            textNoMessage.setVisibility(View.GONE);
                            listConvention.setVisibility(View.VISIBLE);
                        } else {
                            progressBar.setVisibility(View.GONE);
                            textNoMessage.setVisibility(View.VISIBLE);
                        }
                    }
                }));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        conventionAdapter.release();
    }
}

