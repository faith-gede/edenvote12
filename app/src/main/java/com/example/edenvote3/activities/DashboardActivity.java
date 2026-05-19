package com.example.edenvote3.activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.edenvote3.R;
import com.example.edenvote3.adapters.ElectionAdapter;
import com.example.edenvote3.database.DatabaseHelper;
import com.example.edenvote3.models.Election;
import com.example.edenvote3.utils.SessionManager;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {
    TextView tvWelcome, tvActiveCount, tvVotedCount, tvUpcomingCount;
    RecyclerView rvElections;
    TabLayout tabLayoutFilters;
    ElectionAdapter electionAdapter;
    DatabaseHelper dbHelper;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        dbHelper = new DatabaseHelper(this);
        session = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("EdenVote Dashboard");
        }

        tvWelcome = findViewById(R.id.tvWelcome);
        tvActiveCount = findViewById(R.id.tvActiveCount);
        tvVotedCount = findViewById(R.id.tvVotedCount);
        tvUpcomingCount = findViewById(R.id.tvUpcomingCount);
        rvElections = findViewById(R.id.rvElections);
        tabLayoutFilters = findViewById(R.id.tabLayoutFilters);

        tvWelcome.setText("Welcome, " + session.getFullName() + "!");

        rvElections.setLayoutManager(new LinearLayoutManager(this));
        electionAdapter = new ElectionAdapter(this, new ArrayList<>());
        rvElections.setAdapter(electionAdapter);

        tabLayoutFilters.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadElections(tab.getPosition());
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        loadStats();
        loadElections(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStats();
        loadElections(tabLayoutFilters.getSelectedTabPosition());
    }

    private void loadStats() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        Cursor cActive = db.rawQuery("SELECT COUNT(*) FROM elections WHERE status='active'", null);
        if (cActive.moveToFirst()) tvActiveCount.setText(String.valueOf(cActive.getInt(0)));
        cActive.close();

        Cursor cVoted = db.rawQuery("SELECT COUNT(*) FROM votes WHERE voter_id=?", new String[]{String.valueOf(session.getUserId())});
        if (cVoted.moveToFirst()) tvVotedCount.setText(String.valueOf(cVoted.getInt(0)));
        cVoted.close();

        Cursor cUpcoming = db.rawQuery("SELECT COUNT(*) FROM elections WHERE status='upcoming'", null);
        if (cUpcoming.moveToFirst()) tvUpcomingCount.setText(String.valueOf(cUpcoming.getInt(0)));
        cUpcoming.close();
    }

    private void loadElections(int filterIndex) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query;
        String[] args = null;

        String baseQuery = "SELECT e.id, e.title, e.description, e.start_date, e.end_date, e.status, " +
                "(SELECT COUNT(*) FROM candidates c WHERE c.election_id = e.id) as cand_count, " +
                "(SELECT COUNT(*) FROM votes v WHERE v.election_id = e.id) as vote_count " +
                "FROM elections e ";

        switch (filterIndex) {
            case 1: // Active Now
                query = baseQuery + "WHERE e.status = 'active' ORDER BY e.start_date DESC";
                break;
            case 2: // Upcoming
                query = baseQuery + "WHERE e.status = 'upcoming' ORDER BY e.start_date DESC";
                break;
            case 3: // Completed
                query = baseQuery + "WHERE e.status = 'completed' ORDER BY e.start_date DESC";
                break;
            default: // All Elections
                query = baseQuery + "WHERE e.status != 'draft' ORDER BY e.start_date DESC";
                break;
        }

        Cursor cursor = db.rawQuery(query, args);
        List<Election> elections = new ArrayList<>();
        while (cursor.moveToNext()) {
            Election e = new Election();
            e.id = cursor.getInt(0);
            e.title = cursor.getString(1);
            e.description = cursor.getString(2);
            e.startDate = cursor.getString(3);
            e.endDate = cursor.getString(4);
            e.status = cursor.getString(5);
            e.candidateCount = cursor.getInt(6);
            e.voteCount = cursor.getInt(7);
            elections.add(e);
        }
        cursor.close();
        electionAdapter.setData(elections);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            session.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        } else if (id == R.id.action_faq) {
            startActivity(new Intent(this, FaqActivity.class));
            return true;
        } else if (id == R.id.action_contact) {
            startActivity(new Intent(this, ContactActivity.class));
            return true;
        } else if (id == R.id.action_results) {
            startActivity(new Intent(this, ResultsActivity.class));
            return true;
        } else if (id == R.id.action_candidates) {
            startActivity(new Intent(this, CandidatesActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
