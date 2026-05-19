package com.example.edenvote3.fragments;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.edenvote3.R;
import com.example.edenvote3.database.DatabaseHelper;
import com.example.edenvote3.models.Candidate;
import com.example.edenvote3.models.Election;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class CandidatesFragment extends Fragment {
    private RecyclerView rvCandidates;
    private DatabaseHelper dbHelper;
    private AdminCandidateAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_candidates, container, false);
        dbHelper = new DatabaseHelper(getContext());
        rvCandidates = view.findViewById(R.id.rvAdminCandidates);
        FloatingActionButton fab = view.findViewById(R.id.fabAddCandidate);

        rvElectionsReady();

        rvCandidates.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AdminCandidateAdapter(new ArrayList<>());
        rvCandidates.setAdapter(adapter);

        fab.setOnClickListener(v -> showAddCandidateDialog());

        loadCandidates();
        return view;
    }

    private void rvElectionsReady() {
        // Ensure elections exist before adding candidates
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM elections", null);
        if (c.moveToFirst() && c.getInt(0) == 0) {
            Toast.makeText(getContext(), "Please add an election first", Toast.LENGTH_SHORT).show();
        }
        c.close();
    }

    private void loadCandidates() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, full_name, major, year_level, bio, election_id, position_title FROM candidates ORDER BY id DESC", null);
        List<Candidate> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            Candidate c = new Candidate();
            c.id = cursor.getInt(0);
            c.fullName = cursor.getString(1);
            c.major = cursor.getString(2);
            c.yearLevel = cursor.getString(3);
            c.bio = cursor.getString(4);
            c.electionId = cursor.getInt(5);
            c.positionTitle = cursor.getString(6);
            list.add(c);
        }
        cursor.close();
        adapter.setData(list);
    }

    private void showAddCandidateDialog() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_candidate, null);
        Spinner spinner = view.findViewById(R.id.spinnerElections);
        EditText etName = view.findViewById(R.id.etCandidateName);
        EditText etMajor = view.findViewById(R.id.etMajor);
        EditText etYear = view.findViewById(R.id.etYearLevel);
        EditText etBio = view.findViewById(R.id.etBio);

        List<Election> elections = new ArrayList<>();
        SQLiteDatabase dbRead = dbHelper.getReadableDatabase();
        Cursor c = dbRead.rawQuery("SELECT id, title FROM elections", null);
        List<String> titles = new ArrayList<>();
        while (c.moveToNext()) {
            Election e = new Election();
            e.id = c.getInt(0);
            e.title = c.getString(1);
            elections.add(e);
            titles.add(e.title);
        }
        c.close();

        if (elections.isEmpty()) {
            Toast.makeText(getContext(), "Please create an election first", Toast.LENGTH_LONG).show();
            return;
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, titles);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        new AlertDialog.Builder(getContext())
            .setTitle("Add Candidate")
            .setView(view)
            .setPositiveButton("Add", (dialog, which) -> {
                int electionId = elections.get(spinner.getSelectedItemPosition()).id;
                String name = etName.getText().toString().trim();
                String major = etMajor.getText().toString().trim();
                String year = etYear.getText().toString().trim();
                String bio = etBio.getText().toString().trim();

                if (name.isEmpty()) {
                    Toast.makeText(getContext(), "Name is required", Toast.LENGTH_SHORT).show();
                    return;
                }

                SQLiteDatabase dbWrite = dbHelper.getWritableDatabase();
                ContentValues v = new ContentValues();
                v.put("election_id", electionId);
                v.put("full_name", name);
                v.put("major", major);
                v.put("year_level", year);
                v.put("bio", bio);
                v.put("position_title", elections.get(spinner.getSelectedItemPosition()).title);
                dbWrite.insert("candidates", null, v);
                loadCandidates();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showEditCandidateDialog(Candidate candidate) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_candidate, null);
        Spinner spinner = view.findViewById(R.id.spinnerElections);
        EditText etName = view.findViewById(R.id.etCandidateName);
        EditText etMajor = view.findViewById(R.id.etMajor);
        EditText etYear = view.findViewById(R.id.etYearLevel);
        EditText etBio = view.findViewById(R.id.etBio);

        etName.setText(candidate.fullName);
        etMajor.setText(candidate.major);
        etYear.setText(candidate.yearLevel);
        etBio.setText(candidate.bio);

        List<Election> elections = new ArrayList<>();
        SQLiteDatabase dbRead = dbHelper.getReadableDatabase();
        Cursor c = dbRead.rawQuery("SELECT id, title FROM elections", null);
        List<String> titles = new ArrayList<>();
        int selectedIndex = 0;
        while (c.moveToNext()) {
            Election e = new Election();
            e.id = c.getInt(0);
            e.title = c.getString(1);
            if (e.id == candidate.electionId) selectedIndex = elections.size();
            elections.add(e);
            titles.add(e.title);
        }
        c.close();

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, titles);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setSelection(selectedIndex);

        new AlertDialog.Builder(getContext())
            .setTitle("Edit Candidate")
            .setView(view)
            .setPositiveButton("Update", (dialog, which) -> {
                int electionId = elections.get(spinner.getSelectedItemPosition()).id;
                String name = etName.getText().toString().trim();
                String major = etMajor.getText().toString().trim();
                String year = etYear.getText().toString().trim();
                String bio = etBio.getText().toString().trim();

                if (name.isEmpty()) {
                    Toast.makeText(getContext(), "Name is required", Toast.LENGTH_SHORT).show();
                    return;
                }

                SQLiteDatabase dbWrite = dbHelper.getWritableDatabase();
                ContentValues v = new ContentValues();
                v.put("election_id", electionId);
                v.put("full_name", name);
                v.put("major", major);
                v.put("year_level", year);
                v.put("bio", bio);
                v.put("position_title", elections.get(spinner.getSelectedItemPosition()).title);
                
                dbWrite.update("candidates", v, "id=?", new String[]{String.valueOf(candidate.id)});
                loadCandidates();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private class AdminCandidateAdapter extends RecyclerView.Adapter<AdminCandidateAdapter.ViewHolder> {
        private List<Candidate> data;
        AdminCandidateAdapter(List<Candidate> data) { this.data = data; }
        void setData(List<Candidate> data) { this.data = data; notifyDataSetChanged(); }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_candidate_admin, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Candidate c = data.get(position);
            holder.tvName.setText(c.fullName);
            holder.tvRole.setText(c.positionTitle != null ? c.positionTitle : "Candidate");
            
            String details = "";
            if (c.major != null && !c.major.isEmpty() && !"null".equals(c.major)) details += c.major;
            if (c.yearLevel != null && !c.yearLevel.isEmpty() && !"null".equals(c.yearLevel)) {
                if (!details.isEmpty()) details += " | ";
                details += c.yearLevel;
            }
            holder.tvDetails.setText(details.isEmpty() ? "No details provided" : details);

            holder.btnEdit.setOnClickListener(v -> showEditCandidateDialog(c));
        }

        @Override public int getItemCount() { return data.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvRole, tvDetails;
            Button btnEdit;
            ViewHolder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvCandidateName);
                tvRole = v.findViewById(R.id.tvCandidateRole);
                tvDetails = v.findViewById(R.id.tvCandidateDetails);
                btnEdit = v.findViewById(R.id.btnEditCandidate);
            }
        }
    }
}
