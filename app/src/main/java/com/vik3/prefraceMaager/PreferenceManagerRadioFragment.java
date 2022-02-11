package com.vik3.prefraceMaager;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import com.vik3.ui.activities.LogInActivity;
import com.vik3.ui.models.ModelLogInData;
import com.vik3.ui.models.ModelStations;

public class PreferenceManagerRadioFragment {

    // Context
    Context _context;

    /**
     * Preference
     */
    public static final String PREF_SONG_HISTORY = "PREF_LOG_IN";

    /**
     * User Personal Detail
     */
    private static final String MODEL_HISTORY = "model_history";

    /**
     * Constructor
     * @param context
     * current Activity
     */
    public PreferenceManagerRadioFragment(Context context) {
        this._context = context;
    }

    public void setPrefData(ModelStations model){
        String json = new Gson().toJson(model);
        _context.getSharedPreferences(PREF_SONG_HISTORY, MODE_PRIVATE).edit()
                .putString(MODEL_HISTORY, json)
                .apply();
    }

    /**
     * Provides User Data
     * @return User personal detail
     */
    public ModelStations getPrefData(){
        String json = _context.getSharedPreferences(PREF_SONG_HISTORY, MODE_PRIVATE).getString(MODEL_HISTORY, null);
        return new Gson().fromJson(json, ModelStations.class);
    }

    /**
     * Log Out
     */
    public void logOut() {
        _context.getSharedPreferences(PREF_SONG_HISTORY, MODE_PRIVATE).edit().clear().apply();
        _context.startActivity(new Intent(_context, LogInActivity.class));
//        lastActivity(_context, LogInActivity.class);
    }
}
