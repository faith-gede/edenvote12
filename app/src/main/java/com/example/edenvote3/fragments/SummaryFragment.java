package com.example.edenvote3.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.example.edenvote3.R;
import com.example.edenvote3.activities.ResultsActivity;
import com.example.edenvote3.database.DatabaseHelper;

public class SummaryFragment extends Fragment {
    private TextView tvUsers, tvElections, tvCandidates, tvVotes;
    private Button btnViewAudit, btnSystemSettings;
    private CardView cardUsers, cardElections, cardCandidates, cardVotes;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_summary, container, false);
        dbHelper = new DatabaseHelper(getContext());

        // Initialize Views
        tvUsers = view.findViewById(R.id.tvTotalUsers);
        tvElections = view.findViewById(R.id.tvTotalElections);
        tvCandidates = view.findViewById(R.id.tvTotalCandidates);
        tvVotes = view.findViewById(R.id.tvTotalVotes);

        cardUsers = view.findViewById(R.id.cardTotalUsers);
        cardElections = view.findViewById(R.id.cardTotalElections);
        cardCandidates = view.findViewById(R.id.cardTotalCandidates);
        cardVotes = view.findViewById(R.id.cardTotalVotes);

        btnViewAudit = view.findViewById(R.id.btnViewAudit);
        btnSystemSettings = view.findViewById(R.id.btnSystemSettings);

        // Set Click Listeners for Navigation
        ViewPager2 viewPager = getActivity().findViewById(R.id.viewPager);

        if (cardUsers != null) cardUsers.setOnClickListener(v -> viewPager.setCurrentItem(3));
        if (cardElections != null) cardElections.setOnClickListener(v -> viewPager.setCurrentItem(1));
        if (cardCandidates != null) cardCandidates.setOnClickListener(v -> viewPager.setCurrentItem(2));
        if (cardVotes != null) cardVotes.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ResultsActivity.class);
            startActivity(intent);
        });

        btnViewAudit.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ResultsActivity.class);
            startActivity(intent);
        });

        btnSystemSettings.setOnClickListener(v -> {
            Toast.makeText(getContext(), "System configuration is restricted to Super Admins.", Toast.LENGTH_SHORT).show();
        });

        loadStats();
        return view;
    }

    private void loadStats() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor c1 = db.rawQuery("SELECT COUNT(*) FROM users WHERE role='voter'", null);
        if (c1.moveToFirst()) tvUsers.setText(String.valueOf(c1.getInt(0)));
        c1.close();

        Cursor c2 = db.rawQuery("SELECT COUNT(*) FROM elections", null);
        if (c2.moveToFirst()) tvElections.setText(String.valueOf(c2.getInt(0)));
        c2.close();

        Cursor c3 = db.rawQuery("SELECT COUNT(*) FROM candidates", null);
        if (c3.moveToFirst()) tvCandidates.setText(String.valueOf(c3.getInt(0)));
        c3.close();

        Cursor c4 = db.rawQuery("SELECT COUNT(*) FROM votes", null);
        if (c4.moveToFirst()) tvVotes.setText(String.valueOf(c4.getInt(0)));
        c4.close();
    }
}
