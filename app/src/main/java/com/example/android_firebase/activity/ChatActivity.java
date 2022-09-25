package com.example.android_firebase.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android_firebase.R;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {
    private String chatUserId;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ImageView backButton, userAvatar;
    private TextView userName;
    private EditText editTextChat;
    private Button sendButton;
    private FlexboxLayout headerChat;
    private FlexboxLayout inputChat;
    private ProgressBar progressBar;
    private String currentRoomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        init();
        listenEvent();
    }

    private void init() {
        backButton = findViewById(R.id.backButton);
        userAvatar = findViewById(R.id.userAvatar);
        userName = findViewById(R.id.userName);
        editTextChat = findViewById(R.id.editTextChat);
        sendButton = findViewById(R.id.buttonSendMess);
        headerChat = findViewById(R.id.headerChat);
        inputChat = findViewById(R.id.inputChat);
        progressBar = findViewById(R.id.progressBar);

        Bundle bundle = getIntent().getExtras();
        if(bundle == null) return;
        chatUserId = (String) bundle.get("chatUserId");
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        handleCreateRoomChat();
    }

    private void handleLoadRoom(String roomId) {
        currentRoomId = roomId;
        db.collection("rooms").document(roomId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()) {
                    ArrayList<String> members = (ArrayList<String>) documentSnapshot.get("members");
                    for(String member: members) {
                        if(!member.equalsIgnoreCase(auth.getCurrentUser().getUid())) {
                            db.collection("users").document(member).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if(documentSnapshot.exists()) {
                                        Glide.with(ChatActivity.this).load(documentSnapshot.getString("photoURL")).into(userAvatar);
                                        userName.setText(documentSnapshot.getString("displayName"));
                                    }
                                    progressBar.setVisibility(View.GONE);
                                    headerChat.setVisibility(View.VISIBLE);
                                    inputChat.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    private void listenEvent() {
        sendButton.setOnClickListener(v -> createNewMessage());
        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void createNewMessage() {
        if(editTextChat.getText().toString().equals("")) return;
        if(currentRoomId == null) return;
        HashMap<String, Object> message = new HashMap<>();
        message.put("text", editTextChat.getText().toString().trim());
        message.put("senderId", auth.getCurrentUser().getUid());
        message.put("createAt", Timestamp.now());
        message.put("roomId", currentRoomId);
        db.collection("messages").add(message);
        editTextChat.setText("");
        hideSoftKeyboard(this);
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

    private void handleCreateRoomChat() {
        FirebaseUser user = auth.getCurrentUser();
        ArrayList<String> memberToRoom = new ArrayList<>();
        memberToRoom.add(chatUserId);
        memberToRoom.add(user.getUid());

        ArrayList<String> memberToRoomReverse = new ArrayList<>();
        memberToRoomReverse.add(user.getUid());
        memberToRoomReverse.add(chatUserId);

        db.collection("rooms").whereEqualTo("members", memberToRoom).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    if(task.getResult().getDocuments().size() > 0) {
                        handleLoadRoom(task.getResult().getDocuments().get(0).getId());
                    } else {
                        handleFindRoom(memberToRoomReverse);
                    }
                }
            }
        });
    }

    private void handleFindRoom(ArrayList<String> members) {
        db.collection("rooms").whereEqualTo("members", members).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    if(task.getResult().getDocuments().size() > 0) {
                        handleLoadRoom(task.getResult().getDocuments().get(0).getId());
                    } else {
                        handleCreateNewRoom(members);
                    }
                }
            }
        });
    }

    private void handleCreateNewRoom(ArrayList<String> members) {
        HashMap<String, Object> rooms = new HashMap<>();
        rooms.put("members", members);
        rooms.put("createAt", Timestamp.now());
        rooms.put("lastMessage", "");
        db.collection("rooms").add(rooms).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                if(documentReference.getId() != null) {
                    handleLoadRoom(documentReference.getId());
                }
            }
        });
    }
}