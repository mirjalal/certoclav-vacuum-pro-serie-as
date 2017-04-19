package com.certoclav.app.menu;


import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.VideoView;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.app.model.CertoclavNavigationbarClean;



public class VideoFullscreenActivity extends Activity  {

	private GridView programGrid;


	



	
	
	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu_fragment_information_video_fullscreen_activity);
		CertoclavNavigationbarClean navigationbar = new CertoclavNavigationbarClean(this);
		navigationbar.setHeadText(getString(R.string.video));
        VideoView videoView = (VideoView) findViewById(R.id.information_video_fullscreen_videoView);
        String videoPath = getIntent().getExtras().getString(AppConstants.INTENT_EXTRA_VIDEOFULLSCREENACTIVITY_VIDEO_PATH);
		
		
        videoView.setVideoPath(videoPath);
        
        videoView.setOnPreparedListener(new OnPreparedListener() {
			
			@Override
			public void onPrepared(MediaPlayer mp) {
				mp.setLooping(true);
				
			}
		});
        videoView.start();







	}
public void onClickImageShare(View view){
	finish();
}
}


