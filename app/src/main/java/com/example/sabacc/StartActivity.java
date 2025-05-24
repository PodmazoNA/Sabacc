package com.example.sabacc;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StartActivity extends AppCompatActivity {
    private TextView tvHello;
    private Button startBtn, hintBtn, exitBtn;
    private FirebaseAuth fireAuth;
    private DatabaseReference fireDatabase;
    private int[] imageHintRes = {
            R.drawable.hint1, R.drawable.hint2, R.drawable.hint33, R.drawable.hint4, R.drawable.hint5
    };
    private int currentImageIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fireAuth = FirebaseAuth.getInstance();
        if(PrefsHelper.isFirstRun(this)){
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        else{
            setContentView(R.layout.activity_start);
        }

        tvHello = findViewById(R.id.tvHello);
        startBtn = findViewById(R.id.staaaaaart);
        hintBtn = findViewById(R.id.btnHint);
        exitBtn = findViewById(R.id.exitBtn);
        if(startBtn != null) {
            startBtn.setOnClickListener(v -> {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            });
        }
        if(hintBtn != null){
            hintBtn.setOnClickListener(v -> showHint());
        }
        fireDatabase = FirebaseDatabase.getInstance().getReference();
        loadUserData();
    }

    private void loadUserData(){
        FirebaseUser currentUser = fireAuth.getCurrentUser();
        tvHello.setText("Рады видеть Вас в нашей кантине,\n" + currentUser.getDisplayName() + "!");
        if(currentUser != null){
            String userId = currentUser.getUid();
            fireDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user = snapshot.getValue(User.class);
                            if(user != null){
                                tvHello.setText("Рады видеть Вас в нашей кантине,\n" + user.getName() + "!");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            tvHello.setText("Ошибка загрузки данных :(");
                        }
                    }
            );
        }
        else{
            tvHello.setText("Рады видеть Вас в нашей кантине,\nГость!");
        }
    }

    private void showHint(){
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.hint);
        int width = (int)(getResources().getDisplayMetrics().widthPixels * 0.9);
        dialog.getWindow().setLayout(width, (int) (width * 1.6));
        ImageView imageV = dialog.findViewById(R.id.imageHint);
        Button btnPrev = dialog.findViewById(R.id.btnPrev);
        Button btnNext = dialog.findViewById(R.id.btnNext);
        Button btnClose = dialog.findViewById(R.id.btnClose);

        imageV.setImageResource(imageHintRes[currentImageIndex]);
        btnPrev.setOnClickListener(v -> {
            if(currentImageIndex > 0){
                currentImageIndex -= 1;
                imageV.setImageResource(imageHintRes[currentImageIndex]);
            }
        });
        btnNext.setOnClickListener(v -> {
            if(currentImageIndex < imageHintRes.length - 1){
                currentImageIndex += 1;
                imageV.setImageResource(imageHintRes[currentImageIndex]);
            }
        });
        btnClose.setOnClickListener(v -> {
            dialog.dismiss();
            currentImageIndex = 0;
        });
        dialog.show();
    }
}