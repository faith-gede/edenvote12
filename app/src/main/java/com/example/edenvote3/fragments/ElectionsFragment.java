package com.example.edenvote3.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.edenvote3.R;
import com.example.edenvote3.adapters.ElectionAdapter;
import com.example.edenvote3.database.DatabaseHelper;
import com.example.edenvote3.models.Election;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ElectionsFragment extends Fragment {
    private RecyclerView rvElections;
    private ElectionAdapter adapter;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_elections, container, false);
        
        dbHelper = new DatabaseHelper(getContext());
        rvElections = view.findViewById(R.id.rvAdminElections);
        FloatingActionButton fab = view.findViewById(R.id.fabAddElection);

        rvElections.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ElectionAdapter(getContext(), new ArrayList<>());
        
        // Enable admin view with edit listener
        adapter.setAdminView(true, this::showEditElectionDialog);
        
        rvElections.setAdapter(adapter);

        fab.setOnClickListener(v -> showAddElectionDialog());

        loadElections();
        return view;
    }

    private void loadElections() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT e.id, e.title, e.description, e.start_date, e.end_date, e.status, " +
                "(SELECT COUNT(*) FROM candidates c WHERE c.election_id = e.id) as cand_count, " +
                "(SELECT COUNT(*) FROM votes v WHERE v.election_id = e.id) as vote_count " +
                "FROM elections e ORDER BY e.id DESC";
        
        Cursor cursor = db.rawQuery(query, null);
        
        List<Election> list = new ArrayList<>();
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
            list.add(e);
        }
        cursor.close();
        adapter.setData(list);
    }

    private void showAddElectionDialog() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_election, null);
        EditText etTitle = view.findViewById(R.id.etTitle);
        EditText etDesc = view.findViewById(R.id.etDescription);
        EditText etStart = view.findViewById(R.id.etStartDate);
        EditText etEnd = view.findViewById(R.id.etEndDate);

        etStart.setFocusable(false);
        etStart.setOnClickListener(v -> showDatePicker(etStart));
        etEnd.setFocusable(false);
        etEnd.setOnClickListener(v -> showDatePicker(etEnd));

        new AlertDialog.Builder(getContext())
            .setTitle("New Election")
            .setView(view)
            .setPositiveButton("Create", (dialog, which) -> {
                String title = etTitle.getText().toString();
                String desc = etDesc.getText().toString();
                String start = etStart.getText().toString();
                String end = etEnd.getText().toString();

                if (title.isEmpty() || start.isEmpty() || end.isEmpty()) {
                    Toast.makeText(getContext(), "Please fill title and dates", Toast.LENGTH_SHORT).show();
                    return;
                }

                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues v = new ContentValues();
                v.put("title", title);
                v.put("description", desc);
                v.put("start_date", start);
                v.put("end_date", end);
                v.put("status", "active");

                db.insert("elections", null, v);
                loadElections();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showEditElectionDialog(Election e) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_election, null);
        EditText etTitle = view.findViewById(R.id.etTitle);
        EditText etDesc = view.findViewById(R.id.etDescription);
        EditText etStart = view.findViewById(R.id.etStartDate);
        EditText etEnd = view.findViewById(R.id.etEndDate);

        etTitle.setText(e.title);
        etDesc.setText(e.description);
        etStart.setText(e.startDate);
        etEnd.setText(e.endDate);

        etStart.setFocusable(false);
        etStart.setOnClickListener(v -> showDatePicker(etStart));
        etEnd.setFocusable(false);
        etEnd.setOnClickListener(v -> showDatePicker(etEnd));

        new AlertDialog.Builder(getContext())
            .setTitle("Edit Election")
            .setView(view)
            .setPositiveButton("Update", (dialog, which) -> {
                String title = etTitle.getText().toString();
                String desc = etDesc.getText().toString();
                String start = etStart.getText().toString();
                String end = etEnd.getText().toString();

                if (title.isEmpty() || start.isEmpty() || end.isEmpty()) {
                    Toast.makeText(getContext(), "Please fill title and dates", Toast.LENGTH_SHORT).show();
                    return;
                }

                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues v = new ContentValues();
                v.put("title", title);
                v.put("description", desc);
                v.put("start_date", start);
                v.put("end_date", end);

                db.update("elections", v, "id=?", new String[]{String.valueOf(e.id)});
                loadElections();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showDatePicker(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year1, month1, dayOfMonth) -> {
            String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            String date = months[month1] + " " + dayOfMonth + ", " + year1;
            editText.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }
}
