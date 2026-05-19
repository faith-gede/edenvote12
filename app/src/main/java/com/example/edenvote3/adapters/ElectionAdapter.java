package com.example.edenvote3.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.edenvote3.R;
import com.example.edenvote3.activities.ResultsActivity;
import com.example.edenvote3.activities.VoteActivity;
import com.example.edenvote3.database.DatabaseHelper;
import com.example.edenvote3.models.Election;
import com.example.edenvote3.utils.SessionManager;
import java.util.List;

public class ElectionAdapter extends RecyclerView.Adapter<ElectionAdapter.ViewHolder> {
    private List<Election> elections;
    private Context context;
    private DatabaseHelper dbHelper;
    private SessionManager session;
    private boolean isAdminView = false;
    private OnElectionEditListener editListener;

    public interface OnElectionEditListener {
        void onEdit(Election election);
    }

    public ElectionAdapter(Context context, List<Election> elections) {
        this.context = context;
        this.elections = elections;
        this.dbHelper = new DatabaseHelper(context);
        this.session = new SessionManager(context);
    }

    public void setAdminView(boolean adminView, OnElectionEditListener listener) {
        this.isAdminView = adminView;
        this.editListener = listener;
        notifyDataSetChanged();
    }

    public void setData(List<Election> data) {
        this.elections = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_election, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Election e = elections.get(position);
        holder.tvTitle.setText(e.title != null ? e.title : "Unnamed Election");
        holder.tvDescription.setText(e.description != null && !e.description.isEmpty() ? e.description : "No description available.");
        holder.tvStartDate.setText(e.startDate != null ? e.startDate : "TBD");
        holder.tvEndDate.setText(e.endDate != null ? e.endDate : "TBD");
        holder.tvCandidateCount.setText(e.candidateCount + " Candidates");
        
        holder.tvTurnoutPercent.setText(e.voteCount + " Votes Cast");

        int iconRes = android.R.drawable.ic_menu_agenda; 
        if (e.title != null) {
            if (e.title.toLowerCase().contains("sports")) {
                iconRes = android.R.drawable.ic_menu_compass; 
            } else if (e.title.toLowerCase().contains("club") || e.title.toLowerCase().contains("environmental")) {
                iconRes = android.R.drawable.ic_menu_myplaces; 
            } else if (e.title.toLowerCase().contains("senate") || e.title.toLowerCase().contains("representative")) {
                iconRes = android.R.drawable.ic_menu_view; 
            }
        }
        holder.ivElectionIcon.setImageResource(iconRes);

        int bgColor, textColor;
        String statusText;

        String status = e.status != null ? e.status.toLowerCase() : "draft";
        switch (status) {
            case "active":
                statusText = "● ACTIVE NOW";
                bgColor = ContextCompat.getColor(context, R.color.status_active_bg);
                textColor = ContextCompat.getColor(context, R.color.status_active_text);
                break;
            case "upcoming":
                statusText = "● UPCOMING";
                bgColor = ContextCompat.getColor(context, R.color.status_upcoming_bg);
                textColor = ContextCompat.getColor(context, R.color.status_upcoming_text);
                break;
            default:
                statusText = "● COMPLETED";
                bgColor = ContextCompat.getColor(context, R.color.status_completed_bg);
                textColor = ContextCompat.getColor(context, R.color.status_completed_text);
        }

        holder.tvStatus.setText(statusText);
        holder.tvStatus.setTextColor(textColor);
        holder.tvStatus.setBackgroundTintList(ColorStateList.valueOf(bgColor));

        if (isAdminView) {
            holder.layoutAdminActions.setVisibility(View.VISIBLE);
            holder.btnVote.setVisibility(View.GONE);
            
            holder.btnAdminEdit.setOnClickListener(v -> {
                if (editListener != null) editListener.onEdit(e);
            });
            
            holder.btnAdminResults.setOnClickListener(v -> {
                Intent intent = new Intent(context, ResultsActivity.class);
                intent.putExtra("election_id", e.id);
                context.startActivity(intent);
            });
        } else {
            holder.layoutAdminActions.setVisibility(View.GONE);
            holder.btnVote.setVisibility(View.VISIBLE);
            
            boolean hasVoted = checkIfUserVoted(e.id);
            if (hasVoted) {
                holder.btnVote.setText("Already Voted");
                holder.btnVote.setEnabled(true);
                holder.btnVote.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.primary)));
                holder.btnVote.setOnClickListener(v -> {
                    Intent intent = new Intent(context, ResultsActivity.class);
                    intent.putExtra("election_id", e.id);
                    context.startActivity(intent);
                });
            } else if ("active".equalsIgnoreCase(e.status)) {
                holder.btnVote.setText("Cast Your Vote");
                holder.btnVote.setEnabled(true);
                holder.btnVote.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.primary)));
                holder.btnVote.setOnClickListener(v -> {
                    Intent intent = new Intent(context, VoteActivity.class);
                    intent.putExtra("election_id", e.id);
                    intent.putExtra("election_title", e.title);
                    context.startActivity(intent);
                });
            } else if ("upcoming".equalsIgnoreCase(e.status)) {
                holder.btnVote.setText("Voting Opens Soon");
                holder.btnVote.setEnabled(false);
                holder.btnVote.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.light_gray)));
                holder.btnVote.setTextColor(ContextCompat.getColor(context, R.color.primary));
            } else {
                holder.btnVote.setText("View Results");
                holder.btnVote.setEnabled(true);
                holder.btnVote.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.primary)));
                holder.btnVote.setOnClickListener(v -> {
                    Intent intent = new Intent(context, ResultsActivity.class);
                    intent.putExtra("election_id", e.id);
                    context.startActivity(intent);
                });
            }
        }
    }

    private boolean checkIfUserVoted(int electionId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = null;
        try {
            c = db.rawQuery("SELECT id FROM votes WHERE election_id=? AND voter_id=?", 
                new String[]{String.valueOf(electionId), String.valueOf(session.getUserId())});
            return c != null && c.getCount() > 0;
        } catch (Exception e) {
            return false;
        } finally {
            if (c != null) c.close();
        }
    }

    @Override
    public int getItemCount() {
        return elections != null ? elections.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvStatus, tvStartDate, tvEndDate, tvCandidateCount, tvTurnoutPercent;
        ImageView ivElectionIcon;
        LinearLayout layoutAdminActions;
        Button btnAdminEdit, btnAdminResults, btnVote;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvStartDate = itemView.findViewById(R.id.tvStartDate);
            tvEndDate = itemView.findViewById(R.id.tvEndDate);
            tvCandidateCount = itemView.findViewById(R.id.tvCandidateCount);
            tvTurnoutPercent = itemView.findViewById(R.id.tvTurnoutPercent);
            ivElectionIcon = itemView.findViewById(R.id.ivElectionIcon);
            layoutAdminActions = itemView.findViewById(R.id.layoutAdminActions);
            btnAdminEdit = itemView.findViewById(R.id.btnAdminEdit);
            btnAdminResults = itemView.findViewById(R.id.btnAdminResults);
            btnVote = itemView.findViewById(R.id.btnVote);
        }
    }
}
