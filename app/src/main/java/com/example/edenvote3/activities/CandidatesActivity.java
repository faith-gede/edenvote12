package com.example.edenvote3.activities;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.edenvote3.R;
import com.example.edenvote3.adapters.CandidateAdapter;
import com.example.edenvote3.database.DatabaseHelper;
import com.example.edenvote3.models.Candidate;
import java.util.ArrayList;
import java.util.List;

public class CandidatesActivity extends AppCompatActivity {
    RecyclerView rvAllCandidates;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_candidates);

        dbHelper = new DatabaseHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("All Candidates");
        }

        rvAllCandidates = findViewById(R.id.rvAllCandidates);
        rvAllCandidates.setLayoutManager(new LinearLayoutManager(this));

        loadAllCandidates();
    }

    private void loadAllCandidates() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT id, full_name, major, year_level, bio, position_title FROM candidates",
            null);

        List<Candidate> candidates = new ArrayList<>();
        while (cursor.moveToNext()) {
            Candidate c = new Candidate();
            c.id = cursor.getInt(0);
            c.fullName = cursor.getString(1);
            c.major = cursor.getString(2);
            c.yearLevel = cursor.getString(3);
            c.bio = cursor.getString(4);
            c.positionTitle = cursor.getString(5);
            candidates.add(c);
        }
        cursor.close();

        // Use null listener as we are just browsing here
        CandidateAdapter adapter = new CandidateAdapter(this, candidates, null);
        rvAllCandidates.setAdapter(adapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
