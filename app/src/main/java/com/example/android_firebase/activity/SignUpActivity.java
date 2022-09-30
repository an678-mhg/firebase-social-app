package com.example.android_firebase.activity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android_firebase.R;
import com.example.android_firebase.api.UploadService;
import com.example.android_firebase.models.CloudinaryResponse;
import com.example.android_firebase.models.RealPathUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {
    private static final int MY_REQUESTED = 10;
    EditText usernameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    Button signUpButton;
    ProgressBar progressBar;
    TextView signInTextView, profileTextView;
    ImageView profileImageView;
    LinearLayout profileLayout;

    FirebaseAuth auth;
    FirebaseFirestore db;
    Uri mUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        init();
        listenEvent();
    }

    void handleSignUp(String email, String username, String password, String confirmPassword) {
        if(validateData(email, username, password, confirmPassword)) {
            if(mUri != null) {
                handleUploadFile(mUri);
            }
        }
    }

    boolean validateData(String email, String username, String password, String confirmPassword) {
        if(username.length() == 0) {
            usernameEditText.setError("Username is required!");
            return false;
        }
        if(username.length() > 30) {
            usernameEditText.setError("Username < 30 characters");
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Email in valid!");
            return false;
        }
        if(password.length() < 8) {
            passwordEditText.setError("Password must be at least 8 characters!");
            return false;
        }
        if(!confirmPassword.equals(password)) {
            confirmPasswordEditText.setError("Password does not match!");
            return false;
        }
        if(mUri == null) {
            Toast.makeText(this, "Please select image profile", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    void handleSignUpWithFireBase(String email, String username, String password, String imageUri) {
        auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
               if(task.isSuccessful()) {
                   FirebaseUser user = auth.getCurrentUser();
                   updateProfile(user, imageUri, username);
                   setUserInFirestore(username, email, imageUri, user.getUid());
                   startMainActivity();
               } else {
                   Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
               }
               changeProgress(false);
            }
        });
    }

    void updateProfile(FirebaseUser user, String avatarUrl, String username) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .setPhotoUri(Uri.parse(avatarUrl))
                .build();
        user.updateProfile(profileUpdates);
    }

    void setUserInFirestore(String displayName, String email, String avatarUrl, String uid) {
        Map<String, Object> user = new HashMap<>();
        user.put("displayName", displayName);
        user.put("email", email);
        user.put("photoURL", avatarUrl);

        db = FirebaseFirestore.getInstance();
        db.collection("users").document(uid).set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d("add user from fire store", "DocumentSnapshot successfully written!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    void changeProgress(boolean isLoading) {
        if(isLoading) {
            signUpButton.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            signUpButton.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }

    void startMainActivity() {
        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
        startActivity(intent);
        finishAffinity();
    }

    void init() {
        usernameEditText = findViewById(R.id.editTextUserName);
        emailEditText = findViewById(R.id.editTextEmail);
        passwordEditText = findViewById(R.id.editTextPassword);
        confirmPasswordEditText = findViewById(R.id.editTextConfirmPassword);
        signUpButton = findViewById(R.id.signUpButton);
        progressBar = findViewById(R.id.progress_bar);
        signInTextView = findViewById(R.id.login_text_view);
        profileImageView = findViewById(R.id.profileImageView);
        profileLayout = findViewById(R.id.profileLayout);
        profileTextView = findViewById(R.id.profileTextView);
    }

    private void onClickRequestPermission() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            openGallery();
            return;
        }

        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            String [] permission = { Manifest.permission.READ_EXTERNAL_STORAGE };
            requestPermissions(permission, MY_REQUESTED);
        }
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(intent.ACTION_GET_CONTENT);
        mIntentActivityResultLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    private ActivityResultLauncher<Intent> mIntentActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();
                        if(intent == null) {
                            return;
                        }
                        Uri uri = intent.getData();
                        mUri = uri;
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            profileTextView.setVisibility(View.GONE);
                            profileImageView.setImageBitmap(bitmap);
                            profileImageView.setVisibility(View.VISIBLE);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private void handleUploadFile(Uri uri) {
        changeProgress(true);
        String upload_presets = "xhkmjqak";
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), upload_presets);
        String strRealPath = RealPathUtil.getRealPath(this, uri);
        File mFile = new File(strRealPath);
        RequestBody file = RequestBody.create(MediaType.parse("multipart/form-data"), mFile);
        MultipartBody.Part fileUpload = MultipartBody.Part.createFormData("file", mFile.getName(), file);
        UploadService.uploadService.uploadImg(fileUpload, requestBody).enqueue(new Callback<CloudinaryResponse>() {
            @Override
            public void onResponse(Call<CloudinaryResponse> call, Response<CloudinaryResponse> response) {
                CloudinaryResponse cloudinaryResponse = response.body();
                if(cloudinaryResponse != null) {
                    handleSignUpWithFireBase(emailEditText.getText().toString(), usernameEditText.getText().toString(), passwordEditText.getText().toString(), cloudinaryResponse.getUrl());
                }
            }

            @Override
            public void onFailure(Call<CloudinaryResponse> call, Throwable t) {
                Toast.makeText(SignUpActivity.this, "Sign up failed", Toast.LENGTH_LONG).show();
                changeProgress(false);
            }
        });
    }

    void listenEvent() {
        signInTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(intent);
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = usernameEditText.getText().toString();
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String confirmPassword = confirmPasswordEditText.getText().toString();
                handleSignUp(email, username, password, confirmPassword);
            }
        });

        profileLayout.setOnClickListener(v -> onClickRequestPermission());
    }
}