package com.example.sabacc;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationActivity extends AppCompatActivity {
    private EditText etName, etEmail, etPassword;
    private Button btnRegistration;
    private FirebaseAuth fireAuth;
    private DatabaseReference fireDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        fireAuth = FirebaseAuth.getInstance();
        fireDatabase = FirebaseDatabase.getInstance().getReference();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegistration = findViewById(R.id.registrationBtn);
        btnRegistration.setOnClickListener(v -> registerUser());
    }

    private void registerUser(){
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        if(name.isEmpty() || email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }
        fireAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if(task.isSuccessful()){
                FirebaseUser user = fireAuth.getCurrentUser();
                UserProfileChangeRequest profileCR = new UserProfileChangeRequest.Builder()
                        .setDisplayName(name).build();
                user.updateProfile(profileCR).addOnCompleteListener(profileTask -> {
                    if(profileTask.isSuccessful()){
                        Log.d("UserInfo", "profile updated");
                    }
                });
                saveUserData(user.getUid(), name, email);
                PrefsHelper.setFirstRun(this, false);
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                startActivity(new Intent(this, StartActivity.class));
                finish();
            }
            else{
                Toast.makeText(this, "Ошибка регистрации: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserData(String userID, String name, String email){
        User user = new User(name, email);
        fireDatabase.child("users").child(userID).setValue(user);
    }
}