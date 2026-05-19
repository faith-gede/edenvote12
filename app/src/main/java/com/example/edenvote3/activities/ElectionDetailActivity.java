package com.example.edenvote3.activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.edenvote3.R;
import com.example.edenvote3.adapters.CandidateAdapter;
import com.example.edenvote3.database.DatabaseHelper;
import com.example.edenvote3.models.Candidate;
import com.example.edenvote3.utils.SessionManager;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import java.util.ArrayList;
import java.util.List;

public class ElectionDetailActivity extends AppCompatActivity {
    private int electionId;
    private String electionTitle;
    private DatabaseHelper dbHelper;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_election_detail);

        dbHelper = new DatabaseHelper(this);
        session = new SessionManager(this);
        electionId = getIntent().getIntExtra("election_id", -1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsingToolbar);
        TextView tvDesc = findViewById(R.id.tvDetailDescription);
        TextView tvStart = findViewById(R.id.tvDetailStart);
        TextView tvEnd = findViewById(R.id.tvDetailEnd);
        RecyclerView rvCandidates = findViewById(R.id.rvDetailCandidates);
        Button btnAction = findViewById(R.id.btnDetailAction);

        loadElectionData(collapsingToolbar, tvDesc, tvStart, tvEnd);
        
        rvCandidates.setLayoutManager(new LinearLayoutManager(this));
        loadCandidates(rvCandidates);

        btnAction.setOnClickListener(v -> {
            Intent intent = new Intent(this, VoteActivity.class);
            intent.putExtra("election_id", electionId);
            intent.putExtra("election_title", electionTitle);
            startActivity(intent);
        });
    }

    private void loadElectionData(CollapsingToolbarLayout ctl, TextView desc, TextView start, TextView end) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT title, description, start_date, end_date FROM elections WHERE id=?", 
            new String[]{String.valueOf(electionId)});
        
        if (cursor.moveToFirst()) {
            electionTitle = cursor.getString(0);
            ctl.setTitle(electionTitle);
            desc.setText(cursor.getString(1));
            start.setText(cursor.getString(2));
            end.setText(cursor.getString(3));
        }
        cursor.close();
    }

    private void loadCandidates(RecyclerView rv) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, full_name, major, year_level, bio FROM candidates WHERE election_id=?", 
            new String[]{String.valueOf(electionId)});
        
        List<Candidate> candidates = new ArrayList<>();
        while (cursor.moveToNext()) {
            Candidate c = new Candidate();
            c.id = cursor.getInt(0);
            c.fullName = cursor.getString(1);
            c.major = cursor.getString(2);
            c.yearLevel = cursor.getString(3);
            c.bio = cursor.getString(4);
            candidates.add(c);
        }
        cursor.close();
        
        CandidateAdapter adapter = new CandidateAdapter(this, candidates, candidate -> {
            // Simply viewing in this screen
            Intent intent = new Intent(this, CandidateDetailActivity.class);
            intent.putExtra("name", candidate.fullName);
            intent.putExtra("info", candidate.major + " | " + candidate.yearLevel);
            intent.putExtra("bio", candidate.bio);
            startActivity(intent);
        });
        rv.setAdapter(adapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
