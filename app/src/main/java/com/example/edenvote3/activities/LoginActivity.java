package com.example.edenvote3.activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.edenvote3.R;
import com.example.edenvote3.database.DatabaseHelper;
import com.example.edenvote3.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {
    TextInputEditText etEmail, etPassword;
    Button btnLogin;
    CheckBox cbRememberMe;
    TextView tvSignup, tvAdminLogin, tvForgotPassword;
    DatabaseHelper dbHelper;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);
        session = new SessionManager(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignup = findViewById(R.id.tvSignup);
        tvAdminLogin = findViewById(R.id.tvAdminLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // Load saved credentials if Remember Me was checked
        if (session.isRememberMe()) {
            etEmail.setText(session.getSavedEmail());
            etPassword.setText(session.getSavedPassword());
            cbRememberMe.setChecked(true);
        }

        btnLogin.setOnClickListener(v -> attemptLogin());
        tvSignup.setOnClickListener(v ->
            startActivity(new Intent(this, SignupActivity.class)));
        tvAdminLogin.setOnClickListener(v ->
            startActivity(new Intent(this, AdminLoginActivity.class)));
        tvForgotPassword.setOnClickListener(v ->
            startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT id, full_name, role FROM users WHERE email=? AND password=?",
            new String[]{email, password});

        if (cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            String role = cursor.getString(2);
            
            // Handle Remember Me
            session.setRememberMe(cbRememberMe.isChecked(), email, password);
            
            session.saveLogin(id, name, email, role);

            Intent intent = "admin".equals(role)
                ? new Intent(this, AdminDashboardActivity.class)
                : new Intent(this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
        }
        cursor.close();
    }
}
