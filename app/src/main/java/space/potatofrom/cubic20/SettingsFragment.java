package space.potatofrom.cubic20;

import android.content.ComponentName;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.equals(getString(R.string.pref_key_start_on_boot))) {
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
}
