package com.certoclav.app.monitor;


import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.certoclav.app.R;
import com.certoclav.app.listener.SensorDataListener;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveData;

public class MonitorCountdownFragment extends Fragment implements SensorDataListener {

    private TextView textNumber;
    private TextView loadingBarText;
    private TextView timeLeft;
    private ProgressBar loadingBar;
    private RelativeLayout loadingBarLayout;
    private MonitorUtils monitorService;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.monitor_countdown_fragment, container, false); //je nach mIten k�nnte man hier anderen Inhalt laden.

        textNumber = rootView.findViewById(R.id.monitor_countdown_number);
        loadingBarText = rootView.findViewById(R.id.progressBarF0FunctionText);
        timeLeft = rootView.findViewById(R.id.monitor_countdown_left);
        loadingBar = rootView.findViewById(R.id.progressBarF0Function);
        loadingBarLayout = rootView.findViewById(R.id.progressBarF0FunctionLayout);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            loadingBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#3297DB")));
        }
        monitorService = new MonitorUtils();
        Autoclave.getInstance().setOnSensorDataListener(this);

        loadingBarLayout.setVisibility(Autoclave.getInstance().getProfile().isF0Enabled() ? View.VISIBLE : View.GONE);
        timeLeft.setVisibility(Autoclave.getInstance().getProfile().isF0Enabled() ? View.GONE : View.VISIBLE);

        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();


    }


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }


    @Override
    public void onSensorDataChange(AutoclaveData data) {
        try {


            int seconds = (int) (Autoclave.getInstance().getSecondsSinceStart() % 60);
            int minutes = (int) ((Autoclave.getInstance().getSecondsSinceStart() / 60) % 60);
            int hours = (int) ((Autoclave.getInstance().getSecondsSinceStart() / 60 / 60) % 24);
            StringBuilder sBuilder = new StringBuilder();
            sBuilder.append(String.format("%02d", hours))
                    .append(":")
                    .append(String.format("%02d", minutes))
                    .append(":")
                    .append(String.format("%02d", seconds));
            textNumber.setText(sBuilder.toString());
            float timeLeftSeconds = Autoclave.getInstance().getTimeOrPercent();
            if (Autoclave.getInstance().getProfile().isF0Enabled()) {
                loadingBar.setProgress((int) (timeLeftSeconds * 10));
                loadingBarText.setText(String.valueOf((int) (timeLeftSeconds)) + "%");

                //Following the progress of Loading bar
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(loadingBarText.getLayoutParams());
                int margin = (loadingBarLayout.getMeasuredWidth() * (int) (timeLeftSeconds * 10)) / 1000 - 35;
                margin = margin < 0 ? 5 : margin;
                lp.setMargins(margin, 0, 0, 0);
                loadingBarText.setLayoutParams(lp);
            } else {
                int timeLeftSecondsInt = (int) timeLeftSeconds;
                timeLeft.setText(String.format("%02d:%02d:%02d",
                        (timeLeftSecondsInt / 60 / 60) % 24,
                        (timeLeftSecondsInt / 60) % 60,
                        timeLeftSecondsInt % 60));
            }

            //loadingBar.getLayoutParams().width = 300 - (3* ((monitorService.getRemainingTime()*100) / monitorService.getAbsoluteTime(Autoclave.getInstance().getProfile())));
        } catch (Exception e) {
            //do nothing
        }
        //loadingBar.requestLayout();

    }


}

