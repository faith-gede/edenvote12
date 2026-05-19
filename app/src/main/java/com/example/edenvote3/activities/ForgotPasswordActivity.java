package com.example.edenvote3.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.edenvote3.R;
import com.google.android.material.textfield.TextInputEditText;

public class ForgotPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        TextInputEditText etEmail = findViewById(R.id.etResetEmail);
        Button btnReset = findViewById(R.id.btnResetPassword);
        TextView tvBack = findViewById(R.id.tvBackToLogin);

        btnReset.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            } else {
                // Placeholder for backend integration
                Toast.makeText(this, "Password reset link sent to " + email, Toast.LENGTH_LONG).show();
                finish();
            }
        });

        tvBack.setOnClickListener(v -> finish());
    }
}
