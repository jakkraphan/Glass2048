package com.topixoft.glass2048.app;

import android.content.SharedPreferences;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by vitalypolonetsky on 3/24/14.
 */
public class StorageManager {

    private static final String GAME_STATE_PREF = "gameState";
    private static final String BEST_SCORE_PREF = "bestScore";

    private final SharedPreferences preferences;

    public StorageManager(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public void clearGameState() {
        preferences.edit().remove(GAME_STATE_PREF).commit();
    }

    public String[] getGameState() {
        Set<String> stringSet = preferences.getStringSet(GAME_STATE_PREF, null);

        if (stringSet != null) {
            return stringSet.toArray(new String[0]);
        }
        return null;
    }

    public int getBestScore() {
        return preferences.getInt(BEST_SCORE_PREF, 0);
    }

    public void setBestScore(int bestScore) {
        preferences.edit().putInt(BEST_SCORE_PREF, bestScore).commit();
    }

    public void setGameState(String[] previousState) {
        preferences.edit().putStringSet(GAME_STATE_PREF, new HashSet<String>(Arrays.asList(previousState))).commit();
    }
}
