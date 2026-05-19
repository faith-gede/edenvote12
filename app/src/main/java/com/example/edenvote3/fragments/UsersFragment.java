package com.example.edenvote3.fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.edenvote3.R;
import com.example.edenvote3.database.DatabaseHelper;
import com.example.edenvote3.models.User;
import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends Fragment {
    private RecyclerView rvUsers;
    private DatabaseHelper dbHelper;
    private UserAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_users, container, false);
        dbHelper = new DatabaseHelper(getContext());
        rvUsers = view.findViewById(R.id.rvAdminUsers);
        
        rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UserAdapter(new ArrayList<>());
        rvUsers.setAdapter(adapter);

        loadUsers();
        return view;
    }

    private void loadUsers() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, student_id, email, full_name, is_active FROM users WHERE role='voter' ORDER BY full_name ASC", null);
        List<User> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            User u = new User();
            u.id = cursor.getInt(0);
            u.studentId = cursor.getString(1);
            u.email = cursor.getString(2);
            u.fullName = cursor.getString(3);
            u.isActive = cursor.getInt(4) == 1;
            list.add(u);
        }
        cursor.close();
        adapter.setData(list);
    }

    private class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
        private List<User> data;
        UserAdapter(List<User> data) { this.data = data; }
        void setData(List<User> data) { this.data = data; notifyDataSetChanged(); }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_admin, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            User u = data.get(position);
            holder.tvName.setText(u.fullName);
            holder.tvDetail.setText(u.studentId + " | " + u.email);
            holder.swStatus.setChecked(u.isActive);
            
            holder.swStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.execSQL("UPDATE users SET is_active = ? WHERE id = ?", new Object[]{isChecked ? 1 : 0, u.id});
                u.isActive = isChecked;
            });
        }

        @Override public int getItemCount() { return data.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvDetail;
            Switch swStatus;
            ViewHolder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvUserName);
                tvDetail = v.findViewById(R.id.tvUserDetail);
                swStatus = v.findViewById(R.id.swUserStatus);
            }
        }
    }
}
