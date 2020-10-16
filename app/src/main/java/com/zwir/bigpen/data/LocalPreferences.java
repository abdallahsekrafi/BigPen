package com.zwir.bigpen.data;

import android.content.Context;
import android.content.SharedPreferences;

public class LocalPreferences {
    private static final String APP_SHARED_PREFS = "bigPenPref";

    private SharedPreferences appSharedPrefs;
    private SharedPreferences.Editor appPrefsEditor;
    private static final String userCachedEmail="cachedEmail";
    private static final String menuTapTarget ="menuTargetPrompt";
    private static final String storageTapTarget ="storageTargetPrompt";

    public LocalPreferences(Context context) {
        this.appSharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS, Context.MODE_PRIVATE);
        this.appPrefsEditor = appSharedPrefs.edit();
    }
    public void setUserCachedEmail(String userEmail){
        appPrefsEditor.putString(userCachedEmail,userEmail);
        appPrefsEditor.apply();
    }
    public String getUserCachedEmail(){
        return appSharedPrefs.getString(userCachedEmail,null);
    }

    public Boolean getMenuTapTarget() {
        return appSharedPrefs.getBoolean(menuTapTarget,false);
    }
    public void setMenuTapTarget(Boolean prompt) {
        appPrefsEditor.putBoolean(menuTapTarget,prompt);
        appPrefsEditor.apply();
    }
    public Boolean getStorageTapTarget() {
        return appSharedPrefs.getBoolean(storageTapTarget,false);
    }
    public void setStorageTapTarget(Boolean prompt) {
        appPrefsEditor.putBoolean(storageTapTarget,prompt);
        appPrefsEditor.apply();
    }
}
