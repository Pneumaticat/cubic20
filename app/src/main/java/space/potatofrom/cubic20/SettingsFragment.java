package space.potatofrom.cubic20;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by kevin on 7/6/16.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}
