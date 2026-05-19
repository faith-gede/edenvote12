package com.example.edenvote3.activities;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.edenvote3.R;
import com.example.edenvote3.database.DatabaseHelper;
import com.example.edenvote3.models.Candidate;
import java.util.ArrayList;
import java.util.List;

public class ResultsActivity extends AppCompatActivity {
    RecyclerView rvResults;
    ProgressBar pbTurnout;
    TextView tvTurnoutPercent, tvTurnoutCount;
    DatabaseHelper dbHelper;
    int electionId;
    int totalVotes = 0;
    int totalUsers = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        dbHelper = new DatabaseHelper(this);
        electionId = getIntent().getIntExtra("election_id", -1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Election Results");
        }

        rvResults = findViewById(R.id.rvResults);
        pbTurnout = findViewById(R.id.pbTurnout);
        tvTurnoutPercent = findViewById(R.id.tvTurnoutPercent);
        tvTurnoutCount = findViewById(R.id.tvTurnoutCount);

        rvResults.setLayoutManager(new LinearLayoutManager(this));

        loadStats();
        loadResults();
    }

    private void loadStats() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        // Total Voters (Users with role 'voter')
        Cursor cUsers = db.rawQuery("SELECT COUNT(*) FROM users WHERE role='voter' AND is_active=1", null);
        if (cUsers.moveToFirst()) totalUsers = cUsers.getInt(0);
        cUsers.close();

        // Total votes for this election
        Cursor cTotal = db.rawQuery("SELECT COUNT(*) FROM votes WHERE election_id=?", new String[]{String.valueOf(electionId)});
        if (cTotal.moveToFirst()) totalVotes = cTotal.getInt(0);
        cTotal.close();

        int percent = totalUsers > 0 ? (totalVotes * 100 / totalUsers) : 0;
        pbTurnout.setProgress(percent);
        tvTurnoutPercent.setText(percent + "%");
        tvTurnoutCount.setText(totalVotes + " of " + totalUsers + " students voted");
    }

    private void loadResults() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT c.id, c.full_name, COUNT(v.id) as vote_count " +
            "FROM candidates c LEFT JOIN votes v ON c.id = v.candidate_id " +
            "WHERE c.election_id=? GROUP BY c.id ORDER BY vote_count DESC",
            new String[]{String.valueOf(electionId)});

        List<Candidate> results = new ArrayList<>();
        while (cursor.moveToNext()) {
            Candidate c = new Candidate();
            c.id = cursor.getInt(0);
            c.fullName = cursor.getString(1);
            c.voteCount = cursor.getInt(2);
            results.add(c);
        }
        cursor.close();

        rvResults.setAdapter(new ResultsAdapter(results));
    }

    class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ViewHolder> {
        List<Candidate> data;
        ResultsAdapter(List<Candidate> data) { this.data = data; }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.item_result, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Candidate c = data.get(position);
            holder.tvName.setText(c.fullName);
            holder.tvVotes.setText(c.voteCount + " votes");
            
            int percent = totalVotes > 0 ? (c.voteCount * 100 / totalVotes) : 0;
            holder.pbVotes.setProgress(percent);
            holder.tvPercent.setText(percent + "%");
        }

        @Override public int getItemCount() { return data.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvVotes, tvPercent;
            ProgressBar pbVotes;
            ViewHolder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvCandidateName);
                tvVotes = v.findViewById(R.id.tvVoteCount);
                tvPercent = v.findViewById(R.id.tvPercentage);
                pbVotes = v.findViewById(R.id.pbVotes);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
