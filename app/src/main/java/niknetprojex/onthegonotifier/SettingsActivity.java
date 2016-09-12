package niknetprojex.onthegonotifier;

        import android.content.Intent;
        import android.content.SharedPreferences;
        import android.os.Bundle;
        import android.preference.MultiSelectListPreference;
        import android.preference.Preference;
        import android.preference.PreferenceActivity;
        import android.preference.PreferenceManager;
        import android.preference.PreferenceFragment;
        import android.preference.EditTextPreference;
        import android.bluetooth.BluetoothAdapter;
        import android.bluetooth.BluetoothDevice;
        import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

        import java.util.Set;
        import java.util.ArrayList;

/**
 * A {@link PreferenceActivity} that presents a set of application settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
        Intent intent = new Intent(this, ServiceFile.class);
        this.startService(intent);
    }

    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once, to initialize the summary (so it shows up before the value
     * is changed.)
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's
        // current value.
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object text) {
        String stringText = text.toString();

        if (preference instanceof MultiSelectListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            MultiSelectListPreference listPreference = (MultiSelectListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringText);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringText);
        }
        return true;
    }

    public static class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.user_settings);

            // populate the list values with bluetooth devices
            final MultiSelectListPreference listPreference = (MultiSelectListPreference) findPreference("multi_choice_prefs");

            // THIS IS REQUIRED IF YOU DON'T HAVE 'entries' and 'entryValues' in your XML
            setListPreferenceData(listPreference);
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        protected static void setListPreferenceData(MultiSelectListPreference lp) {
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter != null) {
                Set<BluetoothDevice> pairedDevices;
                pairedDevices = mBluetoothAdapter.getBondedDevices();
                ArrayList<String> deviceNames = new ArrayList<String>();
                ArrayList<String> deviceAddresses = new ArrayList<String>(); //displaying list of bluetooth devices to connect to
                if (pairedDevices.size() > 0) {
                    CharSequence[] entries, entryValues;
                    for (BluetoothDevice device : pairedDevices) {
                        // Add the name and address to an array adapter to show in a ListView
                        deviceNames.add(device.getName());
                        deviceAddresses.add(device.getAddress());
                    }
                    CharSequence[] entr = deviceNames.toArray(new CharSequence[deviceNames.size()]);
                    CharSequence[] entryVal = deviceAddresses.toArray(new CharSequence[deviceAddresses.size()]);
                    lp.setEntries(entr);
                    lp.setEntryValues(entryVal);
                }
            }
        }
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference prefer = findPreference(key);
            if (prefer instanceof EditTextPreference) {
                EditTextPreference editTextPrefer = (EditTextPreference) prefer;
                prefer.setSummary(editTextPrefer.getText());
            }
            else if (prefer instanceof MultiSelectListPreference) {
                MultiSelectListPreference listPrefer = (MultiSelectListPreference) prefer;

            }
        }
    }
}