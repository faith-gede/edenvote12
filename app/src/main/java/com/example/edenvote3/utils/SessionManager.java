package com.example.edenvote3.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "EdenVoteSession";
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveLogin(int userId, String name, String email, String role) {
        editor.putBoolean("is_logged_in", true);
        editor.putInt("user_id", userId);
        editor.putString("full_name", name);
        editor.putString("email", email);
        editor.putString("role", role);
        editor.apply();
    }

    public void setRememberMe(boolean remember, String email, String password) {
        editor.putBoolean("remember_me", remember);
        if (remember) {
            editor.putString("saved_email", email);
            editor.putString("saved_password", password);
        } else {
            editor.remove("saved_email");
            editor.remove("saved_password");
        }
        editor.apply();
    }

    public boolean isRememberMe() { return prefs.getBoolean("remember_me", false); }
    public String getSavedEmail() { return prefs.getString("saved_email", ""); }
    public String getSavedPassword() { return prefs.getString("saved_password", ""); }

    public boolean isLoggedIn() { return prefs.getBoolean("is_logged_in", false); }
    public boolean isAdmin() { return "admin".equals(prefs.getString("role", "")); }
    public int getUserId() { return prefs.getInt("user_id", -1); }
    public String getFullName() { return prefs.getString("full_name", ""); }
    public String getEmail() { return prefs.getString("email", ""); }

    public void logout() {
        // We only clear the login session, not the "Remember Me" credentials
        boolean remember = isRememberMe();
        String savedEmail = getSavedEmail();
        String savedPass = getSavedPassword();
        
        editor.clear();
        editor.apply();
        
        // Restore remember me after clear
        if (remember) {
            setRememberMe(true, savedEmail, savedPass);
        }
    }
}
