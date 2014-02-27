package com.nilsbo.egofm.fragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

import com.nilsbo.egofm.R;

import static com.nilsbo.egofm.util.FragmentUtils.logUIAction;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 *
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{
    private static final String TAG = "com.nilsbo.egofm.fragments.SettingsFragment";

    private ListPreference streamquality;
    private SharedPreferences sharedPreferences;
    private ListPreference metadataInterval;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        streamquality = (ListPreference) getPreferenceScreen().findPreference("streamquality");
        metadataInterval = (ListPreference) getPreferenceScreen().findPreference("metadata_interval");
        sharedPreferences = getPreferenceScreen().getSharedPreferences();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getView().setBackgroundColor(getResources().getColor(R.color.background_light_grey));

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        displaySummaries();

        if (s.equals("streamquality")) {
            logUIAction(getActivity(), "Preference Streamquality set", sharedPreferences.getString(s, ""));
        } else if (s.equals("metadata_interval")) {
            logUIAction(getActivity(), "Preference Metadata Interval set", sharedPreferences.getString(s, ""));
        }
    }

    private void displaySummaries() {
        final String qualitySummary = sharedPreferences.getString("streamquality", "");
        int indexOfValue = streamquality.findIndexOfValue(qualitySummary);
        CharSequence[] entries = streamquality.getEntries();

        streamquality.setSummary(entries[indexOfValue]);

        final String metadataSummary = sharedPreferences.getString("metadata_interval", "");
        indexOfValue = metadataInterval.findIndexOfValue(metadataSummary);
        entries = metadataInterval.getEntries();

        metadataInterval.setSummary(entries[indexOfValue]);
    }



    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        displaySummaries();
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
