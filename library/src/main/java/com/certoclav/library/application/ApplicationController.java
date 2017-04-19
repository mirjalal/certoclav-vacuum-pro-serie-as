package com.certoclav.library.application;


import java.io.File;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;


/**
 * An application level class visible to all modules within the application. Its
 * purpose is to maintain global state across an application, contain common
 * methods, initialize services and singletons, etc. <br>
 * To access the ApplicationController class instance in any activity, use
 * {@link Activity#getApplicationContext()}
 */
public class ApplicationController extends Application {
//	private static final String TAG = "ApplicationController:";
	private static Context mContext;
	 private static ApplicationController instance;
	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this;
		instance = this;
	}
	
	public static Context getContext(){
        return mContext;
    }


	   public static ApplicationController getInstance() {
	        return instance;
	    }
	
    public void clearApplicationData() {
        File cache = getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib")) {
                    deleteDir(new File(appDir, s));
                    Log.i("TAG", "**************** File /data/data/APP_PACKAGE/" + s + " DELETED *******************");
                }
            }
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }
    
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager 
              = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    } 
    
    
}
