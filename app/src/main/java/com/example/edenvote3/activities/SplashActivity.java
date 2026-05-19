package com.example.edenvote3.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.example.edenvote3.R;
import com.example.edenvote3.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Auto-navigate after 3 seconds
        new Handler().postDelayed(this::navigateNext, 3000);
    }

    private void navigateNext() {
        if (isFinishing()) return;
        
        SessionManager session = new SessionManager(this);
        Intent intent;
        if (session.isLoggedIn()) {
            intent = session.isAdmin()
                ? new Intent(this, AdminDashboardActivity.class)
                : new Intent(this, DashboardActivity.class);
        } else {
            intent = new Intent(this, LoginActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
