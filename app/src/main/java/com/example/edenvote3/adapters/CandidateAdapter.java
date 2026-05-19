package com.example.edenvote3.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.edenvote3.R;
import com.example.edenvote3.models.Candidate;
import java.util.List;

public class CandidateAdapter extends RecyclerView.Adapter<CandidateAdapter.ViewHolder> {
    private List<Candidate> candidates;
    private Context context;
    private int selectedPosition = -1;
    private OnCandidateSelectedListener listener;

    public interface OnCandidateSelectedListener {
        void onSelected(Candidate candidate);
    }

    public CandidateAdapter(Context context, List<Candidate> candidates, OnCandidateSelectedListener listener) {
        this.context = context;
        this.candidates = candidates;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_candidate, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Candidate c = candidates.get(position);
        holder.tvName.setText(c.fullName != null ? c.fullName : "Unknown Candidate");
        
        // Show Position Title prominently in info
        String info = "";
        if (c.positionTitle != null && !c.positionTitle.isEmpty() && !"null".equals(c.positionTitle)) {
            info = c.positionTitle;
        }
        
        String subInfo = "";
        if (c.major != null && !c.major.isEmpty() && !"null".equals(c.major)) {
            subInfo += c.major;
        }
        if (c.yearLevel != null && !c.yearLevel.isEmpty() && !"null".equals(c.yearLevel)) {
            if (!subInfo.isEmpty()) subInfo += " | ";
            subInfo += c.yearLevel;
        }
        
        if (!subInfo.isEmpty()) {
            if (!info.isEmpty()) info += "\n";
            info += subInfo;
        }
        
        if (info.isEmpty()) {
            info = "Candidate Information";
        }
        
        holder.tvInfo.setText(info);
        
        // Only show RadioButton if we are in a selectable context (like VoteActivity)
        holder.rbSelect.setVisibility(listener != null ? View.VISIBLE : View.GONE);
        holder.rbSelect.setChecked(position == selectedPosition);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                selectedPosition = holder.getAdapterPosition();
                notifyDataSetChanged();
                listener.onSelected(c);
            }
        });
    }

    @Override
    public int getItemCount() {
        return candidates != null ? candidates.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvInfo;
        RadioButton rbSelect;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCandidateName);
            tvInfo = itemView.findViewById(R.id.tvCandidateInfo);
            rbSelect = itemView.findViewById(R.id.rbSelect);
        }
    }
}
