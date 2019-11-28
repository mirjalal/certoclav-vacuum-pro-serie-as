/*
 *  This file is part of Language Picker Widget.
 *
 *  Language Picker Widget is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Language Picker Widget is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Language Picker Widget.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.certoclav.app.settings;

import java.util.ArrayList;
import java.util.Locale;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.app.adapters.LanguageAdapter;
import com.certoclav.app.menu.LoginActivity;
import com.certoclav.app.menu.MenuMain;
import com.certoclav.app.model.CertoclavNavigationbarClean;
import com.certoclav.app.util.LocaleHelper;
import com.certoclav.library.certocloud.CloudUser;

/**
 * The configuration screen for the ExampleAppWidgetProvider widget sample.
 */
public class SettingsLanguagePickerActivity extends ListActivity {

    private static final String LOG_TAG = "WidgetConfigure";
    private static final int WARNING_DIALOG = 0;
    private ListView mListView = null;
    private LanguageAdapter languageAdapter = null;

    @Override
    protected Dialog onCreateDialog(int id) {
        Log.d(LOG_TAG, "dialog id='" + id + "'");
        switch (id) {
            case WARNING_DIALOG:
                Log.d(LOG_TAG, "inside warning dialog");
                return new AlertDialog.Builder(SettingsLanguagePickerActivity.this)
                        .setTitle(R.string.language_not_selected)
                        .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        })
                        .create();
        }
        return null;
    }


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e("SettingsLanguageActivity", "oncreate called");
        setResult(RESULT_CANCELED);
        setContentView(R.layout.settings_language_picker_activity);
        CertoclavNavigationbarClean navigationbar = new CertoclavNavigationbarClean(this);
        navigationbar.setHeadText(getString(R.string.title_choose_language));


        populateList();

        mListView = getListView();
        mListView.setItemsCanFocus(false);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);


        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Locale locale = languageAdapter.getItem(position);
                Log.e("LANG ACTIVITY", locale.getLanguage());
//                change_setting_language(locale.getLanguage(), locale.getCountry());

                changeLang(locale.getLanguage());
                Intent intent = null;
                if(!CloudUser.getInstance().isLoggedIn()) {
                    intent = new Intent(getApplicationContext(), LoginActivity.class);
                } else {
                    intent = new Intent(getApplicationContext(), MenuMain.class);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });


        registerForContextMenu(mListView);
        Log.e("SettingsLanguageActivity", "oncreate finished");
    }

    private void changeLang(String lang) {
        LocaleHelper.language = lang;
        LocaleHelper.setLocale(getBaseContext(), LocaleHelper.language);
    }


    @Override
    protected void onResume() {
        SharedPreferences prefs = this.getSharedPreferences("com.certoclav.app", Context.MODE_PRIVATE);
        prefs.edit().putBoolean(AppConstants.PREFERENCE_KEY_LANGUAGE_WAS_SET, true).apply();
        super.onResume();
    }


    /**
     *
     */
    private void populateList() {
        // Insert all system locales
        Log.d(LOG_TAG, "Current locale='" + Locale.getDefault() + "'");

        ArrayList<Locale> localesList = new ArrayList<>();
        localesList.add(new Locale("en"));
        localesList.add(new Locale("de"));
//        localesList.add(new Locale("es"));
//        localesList.add(new Locale("fr"));

        languageAdapter = new LanguageAdapter(this, localesList);
        setListAdapter(languageAdapter);


    }


    public void change_setting_language(String language, String country) {



//        try {
//            Log.e("LANG", language);
//            Log.e("COUNTRY", country);
//            Locale locale = new Locale(language);
//
//            Class amnClass = Class.forName("android.app.ActivityManagerNative");
//            Object amn = null;
//            Configuration config = null;
//
//            // amn = ActivityManagerNative.getDefault();
//            Method methodGetDefault = amnClass.getMethod("getDefault");
//            methodGetDefault.setAccessible(true);
//            amn = methodGetDefault.invoke(amnClass);
//
//            // config = amn.getConfiguration();
//            Method methodGetConfiguration = amnClass.getMethod("getConfiguration");
//            methodGetConfiguration.setAccessible(true);
//            config = (Configuration) methodGetConfiguration.invoke(amn);
//
//            // config.userSetLocale = true;
//            Class configClass = config.getClass();
//            Field f = configClass.getField("userSetLocale");
//            f.setBoolean(config, true);
//
//            // set the locale to the new value
//            config.locale = locale;
//
//            // amn.updateConfiguration(config);
////            Method methodUpdateConfiguration = amnClass.getMethod("updateConfiguration", Configuration.class);
////            methodUpdateConfiguration.setAccessible(true);
////            methodUpdateConfiguration.invoke(amn, config);
//
//
//        } catch (Exception e) {
//            Log.e("error lang change-->", "" + e.getCause());
//        }

    }


}
