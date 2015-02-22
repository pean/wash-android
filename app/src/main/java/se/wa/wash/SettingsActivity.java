package se.wa.wash;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {

    final static String KEY_PREF_WASH_API_URL = "pref_wash_api_url";
    final static String KEY_PREF_WASH_API_TOKEN = "pref_wash_api_token";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: This is not how it should work..
        addPreferencesFromResource(R.xml.preferences);
    }
}
