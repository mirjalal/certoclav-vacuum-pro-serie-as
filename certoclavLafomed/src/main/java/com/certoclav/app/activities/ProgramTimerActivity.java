package com.certoclav.app.activities;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.certoclav.app.R;
import com.certoclav.app.model.Autoclave;

import java.util.Calendar;

public class ProgramTimerActivity extends CertoclavSuperActivity {

    public static final String ARG_PROGRAM_NAME = "argprogramname";
    public static final String ARG_PROGRAM_STARTING_TIME = "argprogramstartingtime";
    private long leftSeconds;

    private Calendar calendar;
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            StringBuilder builder = new StringBuilder();
            long leftSeconds = ProgramTimerActivity.this.leftSeconds;
            long sec = (leftSeconds >= 60 ? leftSeconds % 60 : leftSeconds);
            long min = (leftSeconds = (leftSeconds / 60)) >= 60 ? leftSeconds % 60 : leftSeconds;
            long hrs = (leftSeconds = (leftSeconds / 60)) >= 24 ? leftSeconds % 24 : leftSeconds;
            long days = (leftSeconds = (leftSeconds / 24)) >= 30 ? leftSeconds % 30 : leftSeconds;
            long months = (leftSeconds = (leftSeconds / 30)) >= 12 ? leftSeconds % 12 : leftSeconds;
            long years = (leftSeconds = (leftSeconds / 12));

            builder.append(years > 0 ? (years + "y ") : "");
            builder.append(months > 0 ? (months + "m ") : "");
            builder.append(days > 0 ? (days + "d ") : "");
            builder.append(hrs > 0 ? (hrs + "h ") : "");
            builder.append(min > 0 ? (min + "m ") : "");
            builder.append(sec > 0 ? (sec + "s ") : "");
            textViewRemainTime.setText(builder.toString());
            if (--ProgramTimerActivity.this.leftSeconds < 0) {
                setResult(RESULT_OK);
                finish();
            } else
                handler.postDelayed(this, 1000);

        }
    };
    private TextView textViewRemainTime;
    private TextView textViewProgramName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_program_timer);
        setResult(RESULT_CANCELED);
        textViewProgramName = (TextView) findViewById(R.id.textViewProgramName);
        textViewRemainTime = (TextView) findViewById(R.id.textViewRemainTime);
        leftSeconds = (getIntent().getLongExtra(ARG_PROGRAM_STARTING_TIME, 0) - Autoclave.getInstance().getDateObject().getTime()) / 1000;
        textViewProgramName.setText(getIntent().getStringExtra(ARG_PROGRAM_NAME));
        handler.post(runnable);

    }



    public void onCancel(View view) {
        finish();
    }

    public void onStartNow(View view) {
        setResult(RESULT_OK);
        finish();
    }
}
