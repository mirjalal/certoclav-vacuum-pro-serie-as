package com.certoclav.library.util;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.provider.Settings;
import android.view.WindowManager;

public class SettingsDeviceUtils {

	
	
	public SettingsDeviceUtils() {

	}
	
	


	public void setvolumeToMaximum(Context eContext) {
		AudioManager am = 
			    (AudioManager) eContext.getSystemService(Context.AUDIO_SERVICE);

			am.setStreamVolume(
			    AudioManager.STREAM_MUSIC,
			    am.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
			    0);
			
			am.setStreamVolume(
				    AudioManager.STREAM_SYSTEM,
				    am.getStreamMaxVolume(AudioManager.STREAM_SYSTEM),
				    0);

			am.setStreamVolume(
				    AudioManager.STREAM_ALARM,
				    am.getStreamMaxVolume(AudioManager.STREAM_ALARM),
				    0);
			
			am.setStreamVolume(
				    AudioManager.STREAM_NOTIFICATION,
				    am.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION),
				    0);
			am.setStreamVolume(
				    AudioManager.STREAM_DTMF,
				    am.getStreamMaxVolume(AudioManager.STREAM_DTMF),
				    0);
			am.setStreamVolume(
				    AudioManager.STREAM_RING,
				    am.getStreamMaxVolume(AudioManager.STREAM_RING),
				    0);
			am.setStreamVolume(
				    AudioManager.STREAM_VOICE_CALL,
				    am.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),
				    0);
	}
	
	
	public void setScreenBrightnessToMaximum(Activity eActivity) {

		
		Settings.System.putInt(eActivity.getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
		Settings.System.putInt(eActivity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 255);
				

            //brightness for current window
            //Get the current window attributes
            //Set the brightness of this window
            //Apply attribute changes to this window
        	WindowManager.LayoutParams layout = eActivity.getWindow().getAttributes();
        	layout.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
        	eActivity.getWindow().setAttributes(layout);
 
		
	}



	
}
