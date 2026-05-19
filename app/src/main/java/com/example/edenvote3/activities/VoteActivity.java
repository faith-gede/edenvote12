package com.example.edenvote3.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.edenvote3.R;
import com.example.edenvote3.adapters.CandidateAdapter;
import com.example.edenvote3.database.DatabaseHelper;
import com.example.edenvote3.models.Candidate;
import com.example.edenvote3.utils.SessionManager;
import java.util.ArrayList;
import java.util.List;

public class VoteActivity extends AppCompatActivity {
    TextView tvElectionTitle, tvSelectedCandidate;
    RecyclerView rvCandidates;
    Button btnCastVote;
    DatabaseHelper dbHelper;
    SessionManager session;
    int electionId;
    int selectedCandidateId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote);

        dbHelper = new DatabaseHelper(this);
        session = new SessionManager(this);

        electionId = getIntent().getIntExtra("election_id", -1);
        String title = getIntent().getStringExtra("election_title");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Cast Your Vote");
        }

        tvElectionTitle = findViewById(R.id.tvElectionTitle);
        tvSelectedCandidate = findViewById(R.id.tvSelectedCandidate);
        rvCandidates = findViewById(R.id.rvCandidates);
        btnCastVote = findViewById(R.id.btnCastVote);

        tvElectionTitle.setText(title != null ? title : "Election");

        rvCandidates.setLayoutManager(new LinearLayoutManager(this));
        loadCandidates();

        btnCastVote.setOnClickListener(v -> showConfirmDialog());
    }

    private void loadCandidates() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Added position_title and bio to the query
        Cursor cursor = db.rawQuery(
            "SELECT id, full_name, major, year_level, bio, position_title FROM candidates WHERE election_id=?",
            new String[]{String.valueOf(electionId)});

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

        CandidateAdapter adapter = new CandidateAdapter(this, candidates, candidate -> {
            selectedCandidateId = candidate.id;
            tvSelectedCandidate.setText(candidate.fullName);
            btnCastVote.setEnabled(true);
        });
        rvCandidates.setAdapter(adapter);
    }

    private void showConfirmDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Confirm Vote")
            .setMessage("Are you sure you want to cast your vote for " + tvSelectedCandidate.getText().toString() + "? This action cannot be undone.")
            .setPositiveButton("Cast Vote", (dialog, which) -> submitVote())
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void submitVote() {
        int voterId = session.getUserId();
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Check if already voted
        Cursor c = db.rawQuery(
            "SELECT id FROM votes WHERE election_id=? AND voter_id=?",
            new String[]{String.valueOf(electionId), String.valueOf(voterId)});
        if (c.moveToFirst()) {
            showErrorModal("You have already voted in this election.");
            c.close();
            return;
        }
        c.close();

        ContentValues values = new ContentValues();
        values.put("election_id", electionId);
        values.put("voter_id", voterId);
        values.put("candidate_id", selectedCandidateId);

        long result = db.insert("votes", null, values);
        if (result != -1) {
            showSuccessModal();
        } else {
            showErrorModal("Failed to cast vote. Please try again.");
        }
    }

    private void showSuccessModal() {
        new AlertDialog.Builder(this)
            .setTitle("Success!")
            .setMessage("Your vote has been cast successfully. Thank you for participating!")
            .setCancelable(false)
            .setPositiveButton("View Results", (dialog, which) -> {
                Intent intent = new Intent(this, ResultsActivity.class);
                intent.putExtra("election_id", electionId);
                startActivity(intent);
                finish();
            })
            .show();
    }

    private void showErrorModal(String message) {
        new AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
