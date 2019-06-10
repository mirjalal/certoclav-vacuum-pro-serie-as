package com.certoclav.app.monitor;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.certoclav.app.R;
import com.certoclav.app.activities.CertoclavSuperActivity;
import com.certoclav.app.listener.AlertListener;
import com.certoclav.app.model.AutoclaveMonitor;
import com.certoclav.app.model.Error;
import com.certoclav.app.service.ReadAndParseSerialService;

import java.util.ArrayList;

public class MonitorNotificationActivity extends CertoclavSuperActivity implements AlertListener {

    private LinearLayout notificationContainer = null;

    private LinearLayout notificationHeadContainer = null;
    private TextView textNotificationHead = null;
    private Button buttonOk = null;
    private Button buttonCancel = null;

    private String lastErrorMessage = "";
    private int errorCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.monitor_notification_activity);

        AutoclaveMonitor.getInstance().setOnAlertListener(this);

        notificationContainer = (LinearLayout) findViewById(R.id.monitor_notification_container);
        notificationHeadContainer = (LinearLayout) findViewById(R.id.monitor_notification_headtext_background);
        textNotificationHead = (TextView) findViewById(R.id.monitor_notification_headtext);
        buttonOk = (Button) findViewById(R.id.monitor_btn_ok);
        buttonCancel = (Button) findViewById(R.id.monitor_btn_cancel);
        textNotificationHead.setText(R.string.warning);


        //user can press cancel button to cancel the prepare to run status
        buttonCancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                AutoclaveMonitor.getInstance().cancelPrepareToRun();
                AutoclaveMonitor.getInstance().ignoreErrorsTemporary();
                finish();
            }
        });

        //user can press the ok button in order to confirm that an error happened
        buttonOk.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                AutoclaveMonitor.getInstance().codeEnterded();
                //AutoclaveMonitor.getInstance().cancelPrepareToRun();
//                finish();
                ReadAndParseSerialService.getInstance().confirmError();
            }


        });


    }


    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onWarnListChange(ArrayList<Error> errorList, ArrayList<Error> warningList) {

        ArrayList<Error> errorAndWarningList = new ArrayList<>();
        errorAndWarningList.addAll(errorList);
        errorAndWarningList.addAll(warningList);
        if (errorAndWarningList != null) {
            if (errorAndWarningList.size() == 0) {
                finish();
            } else {
                if (lastErrorMessage.equals("") || !lastErrorMessage.equals(errorAndWarningList.get(0).getMsg()) || errorAndWarningList.size() != errorCount) {

                    errorCount = errorAndWarningList.size();
                    if (errorAndWarningList.get(0).getType() == Error.TYPE_WARNING) {
                        buttonOk.setVisibility(View.GONE);
                        buttonCancel.setVisibility(View.VISIBLE);
                    } else {
                        buttonOk.setVisibility(View.VISIBLE);
                        buttonCancel.setVisibility(View.VISIBLE);
                    }


                    notificationContainer.removeAllViews();
                    for (Error error : errorAndWarningList) {
                        lastErrorMessage = error.getMsg();
                        View view = getLayoutInflater().inflate(R.layout.alert_view, null);
                        TextView tv = (TextView) view.findViewById(R.id.text_message);
                        tv.setText(error.getMsg());
                        notificationContainer.addView(view);
                    }


                }

            }
        }

    }


}

