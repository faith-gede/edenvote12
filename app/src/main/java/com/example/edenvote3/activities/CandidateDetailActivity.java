package com.example.edenvote3.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.edenvote3.R;
import com.google.android.material.appbar.CollapsingToolbarLayout;

public class CandidateDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_candidate_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsingToolbar);
        TextView tvName = findViewById(R.id.tvDetailName);
        TextView tvInfo = findViewById(R.id.tvDetailInfo);
        TextView tvBio = findViewById(R.id.tvDetailBio);
        ImageView ivLarge = findViewById(R.id.ivCandidateLarge);

        String name = getIntent().getStringExtra("name");
        String info = getIntent().getStringExtra("info");
        String bio = getIntent().getStringExtra("bio");

        collapsingToolbar.setTitle(name);
        tvName.setText(name);
        tvInfo.setText(info);
        tvBio.setText(bio);
        
        // In a real app, use Glide to load ivLarge from photoUrl
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
