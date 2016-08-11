package space.potatofrom.cubic20;

import android.content.ComponentName;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import java.util.Map;

/**
 * Created by kevin on 7/6/16.
 */
public class SettingsFragment
        extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs.registerOnSharedPreferenceChangeListener(this);

        Map<String, ?> preferencesMap = prefs.getAll();
        // Iterate through the preference entries and update their summary if
        // they are an instance of EditTextPreference
        for (Map.Entry<String, ?> preferenceKeyValue : preferencesMap.entrySet()) {
            if (findPreference(preferenceKeyValue.getKey()) instanceof EditTextPreference) {
                updateSummary((EditTextPreference) findPreference(preferenceKeyValue.getKey()));
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (findPreference(key) instanceof EditTextPreference) {
            // Update EditTextPreference summary
            updateSummary((EditTextPreference) findPreference(key));
        } else if (key.equals(getString(R.string.pref_key_start_on_boot))) {
            // Enable/disable boot receiver based on preference
            ComponentName bootReceiver = new ComponentName(
                    getActivity(), ReminderBootReceiver.class);
            PackageManager pm = getActivity().getPackageManager();

            boolean isEnabled = prefs.getBoolean(key, false);
            pm.setComponentEnabledSetting(
                    bootReceiver,
                    isEnabled
                            ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                            : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }
    }

    private void updateSummary(EditTextPreference preference) {
        String key = preference.getKey();
        String value = preference.getText();
        if (key.equals(getString(R.string.pref_key_reminder_interval_min))) {
            // Format reminder-interval-min with minutes suffix
            preference.setSummary(getResources().getQuantityString(
                    R.plurals.unit_format_minutes,
                    Integer.valueOf(value),
                    Integer.valueOf(value)));
        } else if (key.equals(getString(R.string.pref_key_reminder_length_sec))) {
            // Format reminder-length-sec with seconds suffix
            preference.setSummary(getResources().getQuantityString(
                    R.plurals.unit_format_seconds,
                    Integer.valueOf(value),
                    Integer.valueOf(value)));
        } else {
            // Generic; not currently used at the moment.
            preference.setSummary(value);
        }
    }
}
