package com.certoclav.app.menu;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.Log;
import com.certoclav.app.settings.SettingsEmailActivity;
import com.teamviewer.sdk.screensharing.api.TVConfigurationID;
import com.teamviewer.sdk.screensharing.api.TVCreationError;
import com.teamviewer.sdk.screensharing.api.TVSession;
import com.teamviewer.sdk.screensharing.api.TVSessionConfiguration;
import com.teamviewer.sdk.screensharing.api.TVSessionCreationCallback;
import com.teamviewer.sdk.screensharing.api.TVSessionFactory;

import cn.pedant.SweetAlert.SweetAlertDialog;
import es.dmoral.toasty.Toasty;

public class MessagesFragment extends Fragment {


    private TVSessionConfiguration.Builder config;

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

        config = new TVSessionConfiguration.Builder(
                new TVConfigurationID(AppConstants.TVConfigurationID));
        if (Autoclave.getInstance().getUser() != null) {
            config.setServiceCaseName(Autoclave.getInstance().getUser().getEmail());
        } else {
            config.setServiceCaseName(getString(R.string.no_user_info));
        }


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
                    showStartScreenShareDialog();
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

    private void startScreenSharing() {
        TVSessionFactory.createTVSession(getActivity(), AppConstants.TVCongigurationToken,
                new TVSessionCreationCallback() {
                    @Override
                    public void onTVSessionCreationSuccess(TVSession session) {
                        session.start(config.build());
                    }

                    @Override
                    public void onTVSessionCreationFailed(TVCreationError error) {
                        Log.e("tv_error", error.name());
                        Toasty.error(getActivity(), getString(R.string.start_screen_share_problem)+error.name(), Toast.LENGTH_SHORT, true).show();
                    }
                });
    }

    private void showStartScreenShareDialog() {
        final SweetAlertDialog dialog = new SweetAlertDialog(getActivity(), R.layout.dialog_ask_descption, SweetAlertDialog.WARNING_TYPE);
        dialog.setContentView(R.layout.dialog_ask_descption);
        dialog.setTitle(R.string.please_choose_one_of_the_following_options);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);


        final EditText editText = (EditText) dialog.findViewById(R.id.editTextDescription);
        Button buttonStart = (Button) dialog.findViewById(R.id.buttonStart);
        buttonStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismissWithAnimation();
                config.setServiceCaseDescription(editText.getText().toString());
                startScreenSharing();
                getActivity().getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                );
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

