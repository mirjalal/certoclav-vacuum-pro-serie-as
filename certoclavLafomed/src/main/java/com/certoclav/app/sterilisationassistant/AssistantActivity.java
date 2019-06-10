package com.certoclav.app.sterilisationassistant;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.app.activities.CertoclavSuperActivity;
import com.certoclav.app.database.Profile;
import com.certoclav.app.listener.AlertListener;
import com.certoclav.app.listener.AutoclaveStateListener;
import com.certoclav.app.listener.ProfileListener;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveMonitor;
import com.certoclav.app.model.AutoclaveState;
import com.certoclav.app.model.CertoclavNavigationbarClean;
import com.certoclav.app.model.Error;
import com.certoclav.app.monitor.MonitorActivity;
import com.certoclav.library.util.FileUtils;

import java.util.ArrayList;


public class AssistantActivity extends CertoclavSuperActivity implements ProfileListener, AlertListener, AutoclaveStateListener {

    private int currentStep = 1;

    private VideoView videoView;
    private TextView textStepDescription;
    private Button buttonNext;
    private FileUtils fileUtils = new FileUtils();

    CertoclavNavigationbarClean navigationbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sterilisation_assistant_activity);
        navigationbar = new CertoclavNavigationbarClean(this);
    }


    @Override
    public void onResume() {
        currentStep = 1;
        Autoclave.getInstance().setOnProfileListener(this);
        Autoclave.getInstance().setOnAutoclaveStateListener(this);
        AutoclaveMonitor.getInstance().setOnAlertListener(this);
        if (Autoclave.getInstance().getProfile() != null) {
            navigationbar.setHeadText(Autoclave.getInstance().getProfile().getName());
        }
        videoView = (VideoView) findViewById(R.id.sterilisation_assistant_videoview);
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                com.certoclav.app.model.Log.e("video", "setOnErrorListener ");
                return true;
            }
        });
        textStepDescription = (TextView) findViewById(R.id.sterilisation_assistant_text_description);
        buttonNext = (Button) findViewById(R.id.sterilisation_assistant_button_next);

        updateUI();
        super.onResume();
    }


    @Override
    public void onPause() {
        Autoclave.getInstance().removeOnProfileListener(this);
        Autoclave.getInstance().removeOnAutoclaveStateListener(this);
        AutoclaveMonitor.getInstance().removeOnAlertListener(this);
        super.onPause();
    }


    @Override
    public void onProfileChange(Profile profile) {

        if (profile != null) {
            navigationbar.setHeadText(profile.getName());
        }

    }

    @Override
    public void onWarnListChange(ArrayList<Error> errorList, ArrayList<Error> waningList) {
    }

    @Override
    public void onAutoclaveStateChange(AutoclaveState state) {
        if (state == AutoclaveState.RUNNING) {
            //		Intent intent = new Intent(AssistantActivity.this,ControlActivity.class );
            //		startActivity(intent);
        }

    }

    public void updateUI() {

        // textStepNumber.setText(Integer.toString(currentStep));

        switch (currentStep) {
            case 1:
                if (Autoclave.getInstance().getData().isDoorClosed() == false) {
                    currentStep++;
                    updateUI();
                    break;
                }
                Log.e("AssistantActivity", "enter case 1");
                videoView.setVideoPath(fileUtils.getVideoDirectory() +getString(R.string.path_video_door_open));
                videoView.setOnPreparedListener(new OnPreparedListener() {

                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.setLooping(true);

                    }
                });
                videoView.start();
                textStepDescription.setText(R.string.open_the_door);
//                buttonNext.setBackgroundResource(R.drawable.button_bg_green_blue);


                break;

            case 2:


                videoView.setVisibility(View.GONE);
                videoView = null;
                videoView = (VideoView) findViewById(R.id.sterilisation_assistant_videoview);
                videoView.setVisibility(View.VISIBLE);
                videoView.setVideoPath(fileUtils.getVideoDirectory() + getString(R.string.path_video_place_item));
                videoView.setOnPreparedListener(new OnPreparedListener() {

                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.setLooping(true);

                    }
                });


                videoView.start();
                textStepDescription.setText(R.string.video_place_item_descripton);
                break;


            case 3:


                videoView.setVisibility(View.GONE);
                videoView = null;
                videoView = (VideoView) findViewById(R.id.sterilisation_assistant_videoview);
                videoView.setVisibility(View.VISIBLE);
                videoView.setVideoPath(fileUtils.getVideoDirectory() +getString(R.string.path_video_door_close));
                videoView.setOnPreparedListener(new OnPreparedListener() {

                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.setLooping(true);

                    }
                });


                videoView.start();
                textStepDescription.setText(R.string.video_lock_description);
//                buttonNext.setBackgroundResource(R.drawable.button_bg_green_blue);

                break;


            case 4:

                int programIndex = Autoclave.getInstance().getProfile().getIndex();

                videoView.setVisibility(View.GONE);
                videoView = null;
                videoView = (VideoView) findViewById(R.id.sterilisation_assistant_videoview);
                videoView.setVisibility(View.VISIBLE);
                switch (programIndex){
                    case 1: videoView.setVideoPath(getString(R.string.path_video_start_program_1)); break;
                    case 2: videoView.setVideoPath(getString(R.string.path_video_start_program_2)); break;
                    case 3: videoView.setVideoPath(getString(R.string.path_video_start_program_3)); break;
                    case 4: videoView.setVideoPath(getString(R.string.path_video_start_program_4)); break;
                    case 5: videoView.setVideoPath(getString(R.string.path_video_start_program_5)); break;
                    case 6: videoView.setVideoPath(getString(R.string.path_video_start_program_6)); break;
                    case 7: videoView.setVideoPath(getString(R.string.path_video_start_program_7)); break;
                    case 8: videoView.setVideoPath(getString(R.string.path_video_start_program_8)); break;
                    case 9: videoView.setVideoPath(getString(R.string.path_video_start_program_9)); break;
                    case 10: videoView.setVideoPath(getString(R.string.path_video_start_program_10)); break;
                    case 11: videoView.setVideoPath(getString(R.string.path_video_start_program_11)); break;
                    default: videoView.setVideoPath(getString(R.string.path_video_start_program_1)); break;
                }
                videoView.setOnPreparedListener(new OnPreparedListener() {

                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.setLooping(true);

                    }
                });


                videoView.start();


                textStepDescription.setText(getString(R.string.please_choose_and_start_the_program)
                        + " " + Autoclave.getInstance().getProfile().getName() );
                buttonNext.setVisibility(View.GONE);
                break;
        }


    }

    public void onClickNextStep(View view) {

        if ((AppConstants.IS_CERTOASSISTANT == false && currentStep >= 3) || (AppConstants.IS_CERTOASSISTANT == true && currentStep >= 4)) {
            currentStep = 1;
            Intent intent = new Intent(this, MonitorActivity.class);
            startActivity(intent);
            finish();
        } else {

            currentStep = currentStep + 1;
            Log.e("AssistantActivity", "current step" + currentStep);
            updateUI();
        }

    }


}
