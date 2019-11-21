package com.certoclav.app.menu;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import com.certoclav.app.R;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.settings.SettingsEmailActivity;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class MessagesFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.menu_fragment_information_messages, container, false);
        rootView.findViewById(R.id.card_view).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    showSupportDialog();
                } else {
                    Intent intent = new Intent(getActivity(), SettingsEmailActivity.class);
                    getActivity().startActivity(intent);
                }
            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();


    }

    private void showSupportDialog() {
        final SweetAlertDialog dialog = new SweetAlertDialog(getActivity(), R.layout.dialog_select_support, SweetAlertDialog.WARNING_TYPE);
        dialog.setContentView(R.layout.dialog_select_support);
        dialog.setTitle(R.string.please_choose_one_of_the_following_options);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);


        Button buttonLabel = (Button) dialog.findViewById(R.id.dialogButtonSendEmail);
        buttonLabel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismissWithAnimation();
                Intent intent = new Intent(getActivity(), SettingsEmailActivity.class);
                getActivity().startActivity(intent);
            }
        });


        Button buttonShareScreen = (Button) dialog.findViewById(R.id.dialogButtonShareScreen);
        buttonShareScreen.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    dialog.dismissWithAnimation();
                    // open teamviewer quick support apk
                    if(!Autoclave.getInstance().isFDAEnabled()) {
                        Intent launchIntent = getContext().getPackageManager().getLaunchIntentForPackage("com.teamviewer.quicksupport.market");
                        if (launchIntent != null) {
                            startActivity(launchIntent);
                        }
                    } else
                        Toast.makeText(getContext(), getResources().getString(R.string.disable_fda), Toast.LENGTH_LONG).show();

                } catch (Exception e) {
                    Toast.makeText(getActivity(), getString(R.string.please_select_a_protocol_first), Toast.LENGTH_LONG).show();
                }

            }
        });

        Button buttonSendEmail = (Button) dialog.findViewById(R.id.dialogButtonCancel);
        buttonSendEmail.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismissWithAnimation();


            }
        });

        dialog.show();
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
}