package com.certoclav.app.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import java.util.Locale;

public class LocaleHelper {

    public static String language = "en";

    public static Context onAttach(Context context) {
        String langPref = "Language";
        SharedPreferences prefs = context.getSharedPreferences("LangPreference", Activity.MODE_PRIVATE);
        language = prefs.getString(langPref, "en");
        return setLocale(context, language);
    }

    public static Context setLocale(Context context, String language) {
        saveLocale(language, context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResources(context, language);
        } else return updateResourcesLegacy(context, language);
    }

    private static Context updateResourcesLegacy(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration configuration = context.getResources().getConfiguration();
        Resources resources = context.getResources();
        configuration.locale = locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLayoutDirection(locale);
        }
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        return context;
    }

    @TargetApi(Build.VERSION_CODES.N)
    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);
        context.getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());
        context.createConfigurationContext(configuration);
        return context.createConfigurationContext(configuration);
    }

    private static void saveLocale(String language, Context context) {
        String langPref = "Language";
        SharedPreferences prefs = context.getSharedPreferences("LangPreference", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(langPref, language);
        editor.apply();
    }

}
