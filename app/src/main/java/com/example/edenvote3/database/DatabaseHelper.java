package com.example.edenvote3.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "edenvote.db";
    private static final int DB_VERSION = 8; // Bumped version to force data refresh

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "student_id TEXT UNIQUE NOT NULL," +
            "email TEXT UNIQUE NOT NULL," +
            "full_name TEXT NOT NULL," +
            "password TEXT NOT NULL," +
            "role TEXT DEFAULT 'voter'," +
            "is_active INTEGER DEFAULT 1," +
            "created_at TEXT DEFAULT CURRENT_TIMESTAMP)");

        db.execSQL("CREATE TABLE elections (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "title TEXT NOT NULL," +
            "description TEXT," +
            "start_date TEXT NOT NULL," +
            "end_date TEXT NOT NULL," +
            "status TEXT DEFAULT 'draft'," +
            "created_at TEXT DEFAULT CURRENT_TIMESTAMP)");

        db.execSQL("CREATE TABLE candidates (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "election_id INTEGER NOT NULL," +
            "full_name TEXT NOT NULL," +
            "position_title TEXT," +
            "major TEXT," +
            "year_level TEXT," +
            "bio TEXT," +
            "photo_url TEXT," +
            "FOREIGN KEY(election_id) REFERENCES elections(id))");

        db.execSQL("CREATE TABLE votes (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "election_id INTEGER NOT NULL," +
            "voter_id INTEGER NOT NULL," +
            "candidate_id INTEGER NOT NULL," +
            "voted_at TEXT DEFAULT CURRENT_TIMESTAMP," +
            "UNIQUE(election_id, voter_id))");

        seedData(db);
    }

    private void seedData(SQLiteDatabase db) {
        // Use ContentValues for safer and more reliable seeding
        ContentValues admin = new ContentValues();
        admin.put("student_id", "ADMIN001");
        admin.put("email", "admin@eden.edu");
        admin.put("full_name", "Administrator");
        admin.put("password", "admin123");
        admin.put("role", "admin");
        db.insert("users", null, admin);

        // Seeding Elections
        long e1 = insertElection(db, 1, "Student Union President Election", "Main election for student leadership.", "Apr 20, 2026", "May 05, 2026", "active");
        long e2 = insertElection(db, 2, "Class Representative Election", "Vote for your class representative.", "Apr 25, 2026", "May 02, 2026", "active");
        long e3 = insertElection(db, 3, "Hostel Manager Selection", "Annual hostel management election.", "May 15, 2026", "May 20, 2026", "upcoming");

        // Seeding Candidates
        insertCandidate(db, e1, "Willard Kateera", "Student Union President", "Political Science", "Final Year", "Leading with integrity and vision for all students.");
        insertCandidate(db, e2, "Nyasha Chikwizo", "Class Representative", "Business Admin", "Level 2", "Dedicated to being your voice in the academic council.");
        insertCandidate(db, e3, "Faith Gede", "Hostel Manager", "Information Technology", "Level 3", "Committed to improving hostel living standards and security.");
    }

    private long insertElection(SQLiteDatabase db, int id, String title, String desc, String start, String end, String status) {
        ContentValues v = new ContentValues();
        v.put("id", id);
        v.put("title", title);
        v.put("description", desc);
        v.put("start_date", start);
        v.put("end_date", end);
        v.put("status", status);
        return db.insert("elections", null, v);
    }

    private void insertCandidate(SQLiteDatabase db, long electionId, String name, String pos, String major, String year, String bio) {
        ContentValues v = new ContentValues();
        v.put("election_id", electionId);
        v.put("full_name", name);
        v.put("position_title", pos);
        v.put("major", major);
        v.put("year_level", year);
        v.put("bio", bio);
        db.insert("candidates", null, v);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS votes");
        db.execSQL("DROP TABLE IF EXISTS candidates");
        db.execSQL("DROP TABLE IF EXISTS elections");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }
}
