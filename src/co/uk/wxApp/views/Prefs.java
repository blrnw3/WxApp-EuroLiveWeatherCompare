package co.uk.wxApp.views;

import co.uk.wxApp.R;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.widget.Toast;

/**
 * Application Preferences GUI and setter
 * @author Ben LR
 */
public class Prefs extends PreferenceActivity {
	
	private String[] preferenceKeys = { "temp", "rain", "pres" };
	private String[][] keyValues = { { "C", "mm", "mb" }, { "F", "in", "inHg" } };
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//---load the preferences from an XML file---
		addPreferencesFromResource(R.xml.prefs);
		
		for(int i = 0; i < preferenceKeys.length; i++) {
			showToastOnSettingChange( preferenceKeys[i], i );
		}
		
		showToastOnSettingChange("wind", -1);
		showToastOnSettingChange("freq", -1);

	}
	
	private void showToastOnSettingChange(String key,int type) {
		ListPreference lpUpdates = (ListPreference) findPreference(key);
		lpUpdates.setOnPreferenceChangeListener(new MyPrefChangeListener(type));
		
	}
	
	private class MyPrefChangeListener implements OnPreferenceChangeListener {
		
		private int key;

		public MyPrefChangeListener(int key) {
			this.key = key;
		}
		
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			String unit = (key < 0) ? newValue.toString() :
				keyValues[ Boolean.parseBoolean(newValue.toString()) ? 0 : 1 ][key];
			Toast.makeText(Prefs.this, preference.getTitle() + " unit updated to \n" +
					unit , Toast.LENGTH_SHORT).show();
			return true;
		}
		
	}
}
