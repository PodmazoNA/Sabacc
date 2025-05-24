package com.example.sabacc;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegister;
    private FirebaseAuth fireAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        fireAuth = FirebaseAuth.getInstance();
        etEmail = findViewById(R.id.etLogEmail);
        etPassword = findViewById(R.id.etLogPassword);
        btnLogin = findViewById(R.id.vhod);
        btnRegister = findViewById(R.id.registrationLogBtn);

        btnLogin.setOnClickListener(v -> loginUser());
        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegistrationActivity.class));
            finish();
        });
    }

    private void loginUser(){
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }
        fireAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if(task.isSuccessful()){
                PrefsHelper.setFirstRun(this, false);
                PrefsHelper.setLoggedIn(this, true);
                startActivity(new Intent(LoginActivity.this, StartActivity.class));
                finish();
            }
            else{
                Toast.makeText(this, "Ошибка входа: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}