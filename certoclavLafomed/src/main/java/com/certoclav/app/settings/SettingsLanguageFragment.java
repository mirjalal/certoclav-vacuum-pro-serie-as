package com.certoclav.app.settings;




import java.util.Locale;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.support.v4.preference.PreferenceFragment;
import android.util.Log;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;


public class SettingsLanguageFragment extends PreferenceFragment  {
	
private OnSharedPreferenceChangeListener listener;

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("SettingsLanguageFragment", "oncreate called");
        addPreferencesFromResource(R.xml.preferences_language);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());


       ((Preference) findPreference(AppConstants.PREFERENCE_KEY_LANGUAGE)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
		
		@Override
		public boolean onPreferenceClick(Preference preference) {

			Intent i = new Intent();
			i.setAction(android.provider.Settings.ACTION_LOCALE_SETTINGS);
			i.addCategory(Intent.CATEGORY_DEFAULT);
			startActivity(i);
			
        	//Intent intent = new Intent(getActivity(), SettingsLanguagePickerActivity.class);
        	//getActivity().startActivity(intent);
			return false;
		}
	});
   
       

    
       
    listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
      public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
   
      }


    };

    prefs.registerOnSharedPreferenceChangeListener(listener);
       
    Log.e("SettingsLanguageFragment", "oncreate finished");
    }

    

    
    






	@Override
	public void onResume() {

        ((Preference) findPreference(AppConstants.PREFERENCE_KEY_LANGUAGE)).setSummary(String.format("%s (%s)", toTitleCase(Locale.getDefault().getDisplayLanguage()), toTitleCase(Locale.getDefault().getDisplayCountry())));

		super.onResume();
	}


	@Override
	public void onPause() {

		super.onPause();
	}



    private static String toTitleCase(String s) {
        if (s.length() == 0) {
                return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
}
    
}