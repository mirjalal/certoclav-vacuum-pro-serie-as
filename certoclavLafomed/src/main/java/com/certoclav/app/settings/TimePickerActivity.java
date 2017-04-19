package com.certoclav.app.settings;
 
import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.app.listener.NavigationbarListener;
import com.certoclav.app.model.CertoclavNavigationbarClean;
 
public class TimePickerActivity extends Activity implements NavigationbarListener {
 
    private TextView mDateDisplay;
    private View mPickDate;
 
    private int mYear;
    private int mMonth;
    private int mDay;
 
    private TextView mTimeDisplay;
    private View mPickTime;
 
    private int mHour;
    private int mMinute;
 
    static final int DATE_DIALOG_ID = 0;
    static final int CALENDAR_VIEW_ID = 1;
    static final int TIME_DIALOG_ID = 2;
    private CertoclavNavigationbarClean navigationbar;
 

 
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_time_picker_activity);
        navigationbar = new CertoclavNavigationbarClean(this);
        navigationbar.setHeadText(getString(R.string.title_change_date_time));
        // capture our View elements
        mDateDisplay = (TextView) findViewById(R.id.dateDisplay);
        mPickDate =  findViewById(R.id.settings_time_date);
        mTimeDisplay = (TextView) findViewById(R.id.timeDisplay);
        mPickTime = findViewById(R.id.settings_time_time);
 
 
        // add a click listener to the select a date button
        mPickDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });
 

 
        // add a click listener to the button
        mPickTime.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(TIME_DIALOG_ID);
            }
        });
 
 
 
 
        // get the current date and time
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);
 
        // display the current date
        displayDate();

 
        // display the current time
        displayTime();
    }
 

 
    // updates the date in the EditText
    private void displayDate() {
        mDateDisplay.setText(
                new StringBuilder()
                // Month is 0 based so add 1
                .append(mMonth + 1).append("/")
                .append(mDay).append("/")
                .append(mYear).append(" "));
    }
 

 
    // updates the time we display in the EditText
    private void displayTime() {
        mTimeDisplay.setText(
                new StringBuilder()
                .append(pad(mHour)).append(":")
                .append(pad(mMinute)));
    }
 
    private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }
 
    // the callback received when the user "sets" the date in the dialog
    private DatePickerDialog.OnDateSetListener mDateSetListener =
        new DatePickerDialog.OnDateSetListener() {
 
        public void onDateSet(DatePicker view, int year,
                int monthOfYear, int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            displayDate();
            setDateAndTime();
        }
    };
 
    // the callback received when the user "sets" the time in the dialog
    private TimePickerDialog.OnTimeSetListener mTimeSetListener =
        new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mHour = hourOfDay;
            mMinute = minute;
            displayTime();
            setDateAndTime();
        }
    };
 
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DATE_DIALOG_ID:
            return new DatePickerDialog(this,
                    mDateSetListener,
                    mYear, mMonth, mDay);
        case TIME_DIALOG_ID:
            return new TimePickerDialog(this,
                    mTimeSetListener, mHour, mMinute, false);
 
        }
        return null;
    }  
 
	private void setDateAndTime() {
		
		//  ShellInterface shellInterface = new ShellInterface();
        Calendar c = Calendar.getInstance();
        c.set(mYear, mMonth, mDay, mHour, mMinute);
        
	    try
	    {
	     
   

	      String command = "chmod 666 /dev/alarm";
	       Process proc = Runtime.getRuntime().exec(new String[] { "su", "-c", command });//, envp);
	       proc.waitFor();
	       boolean retval = SystemClock.setCurrentTimeMillis(c.getTimeInMillis());
	       Toast.makeText(this, "setting Time " + retval, Toast.LENGTH_LONG).show();
	     //  String command2 = "chmod 664 /dev/alarm";
	     //  Process proc2 = Runtime.getRuntime().exec(new String[] { "su",command2 });//, envp); 
	    }
	    catch(Exception ex)
	    {   if(AppConstants.APPLICATION_DEBUGGING_MODE){
	        Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
	    	}
	    }
		
	}



	@Override
	public void onClickNavigationbarButton(int buttonId) {
		switch(buttonId){
		case CertoclavNavigationbarClean.BUTTON_BACK:
			setResult(RESULT_OK);
			finish();
		break;
		}
		
	}



	@Override
	protected void onResume() {
		navigationbar.setNavigationbarListener(this);
		super.onResume();
	}



	@Override
	protected void onPause() {
		navigationbar.removeNavigationbarListener(this);
		super.onPause();
	}
	
	
	
}