package com.certoclav.app.menu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.certoclav.app.R;
import com.certoclav.app.adapters.ProtocolAdapter;
import com.certoclav.app.button.QuickActionItem;
import com.certoclav.app.database.DatabaseService;
import com.certoclav.app.database.Protocol;
import com.certoclav.app.graph.GraphService;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveMonitor;
import com.certoclav.app.model.ErrorModel;
import com.certoclav.app.monitor.MonitorListFragment;
import com.certoclav.app.service.PostProtocolsService;
import com.certoclav.app.util.Helper;
import com.certoclav.app.util.LabelPrinterUtils;
import com.certoclav.app.util.MyCallback;
import com.certoclav.library.graph.LineGraph;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ProtocolsFragment extends Fragment implements OnClickListener {


    private ListView list;
    ProtocolAdapter protocolAdapter;
    private QuickActionItem actionItemEye;
    private DatabaseService databaseService;
    private TextView textError;
    private Spinner spinner;
    private ArrayAdapter<String> dataAdapter;
    private ProgressBar progressBarProtocolList;
    private ProgressBar progressBarGraph;

    public static final int SPINNER_POSITION_ORDER_BY_START_TIME = 0;
    public static final int SPINNER_POSITION_ORDER_BY_PROGRAM_NAME = 1;
    public static final int SPINNER_POSITION_ORDER_BY_SUCCESS = 2;
    public static final String LIST_GRAPH_PREF = "islistgrapp";

    int aktPosition = 0;
    private CheckBox checkBoxGpaphList;
    private SharedPreferences sharedPreferences;
    private LinearLayout graphContainer;
    private View viewList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.menu_fragment_protocols, container, false); //je nach mIten k�nnte man hier anderen Inhalt laden.

        textError = (TextView) rootView.findViewById(R.id.protocols_text_error);
        textError.setVisibility(View.INVISIBLE);
        progressBarProtocolList = (ProgressBar) rootView.findViewById(R.id.protocols_progress_bar_list);
        progressBarGraph = (ProgressBar) rootView.findViewById(R.id.protocols_progress_bar_graph);
        databaseService = new DatabaseService(getActivity());
        sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        checkBoxGpaphList = ((CheckBox) rootView.findViewById(R.id.checkboxGraphList));
        checkBoxGpaphList.setChecked(sharedPreferences.getBoolean(LIST_GRAPH_PREF, true));

        checkBoxGpaphList.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (graphContainer != null)
                    graphContainer.setVisibility(isChecked ? View.GONE : View.VISIBLE);
                if (viewList != null)
                    viewList.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                sharedPreferences.edit().putBoolean(LIST_GRAPH_PREF, isChecked).commit();

            }
        });

        graphContainer = (LinearLayout) getActivity().findViewById(R.id.protocols_container_graph);
        viewList = getActivity().findViewById(R.id.content);


        rootView.findViewById(R.id.imageViewPrint).setOnClickListener(this);
        rootView.findViewById(R.id.imageViewScan).setOnClickListener(this);


        List<String> listSortstrings = new ArrayList<String>();
        listSortstrings.add(SPINNER_POSITION_ORDER_BY_START_TIME, getActivity().getString(R.string.protocols_sorted_by) + " " + getActivity().getString(R.string.date));
        listSortstrings.add(SPINNER_POSITION_ORDER_BY_PROGRAM_NAME, getActivity().getString(R.string.protocols_sorted_by) + " " + getActivity().getString(R.string.program));
        listSortstrings.add(SPINNER_POSITION_ORDER_BY_SUCCESS, getActivity().getString(R.string.protocols_sorted_by) + " " + getActivity().getString(R.string.success));
        dataAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_dropdown_item_large, listSortstrings);
        dataAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_large);

        spinner = (Spinner) rootView.findViewById(R.id.protocols_spinner_sort);
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {


            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                updateProtocolAdapter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });


        //fill protocoladapter with an empty list. The list will be filled in a thread later
        protocolAdapter = new ProtocolAdapter(getActivity().getApplicationContext(), new ArrayList<Protocol>());
        list = (ListView) rootView.findViewById(R.id.protocols_list);
        try {
            list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            //  list.setSelector(R.color.orange);
        } catch (Exception e) {

        }
        list.setAdapter(protocolAdapter);

        list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int arg2, long arg3) {
                aktPosition = arg2;

                graphContainer = (LinearLayout) getActivity().findViewById(R.id.protocols_container_graph);
                viewList = getActivity().findViewById(R.id.content);
                graphContainer.removeAllViews();

                final LinearLayout graphContainer = (LinearLayout) getActivity().findViewById(R.id.protocols_container_graph);
                graphContainer.removeAllViews();
                progressBarGraph.setVisibility(View.VISIBLE);
                list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                for (int i = 0; i < protocolAdapter.getCount(); i++) {
                    protocolAdapter.getItem(i).setSelected(false);
                }
                protocolAdapter.getItem(arg2).setSelected(true);
                protocolAdapter.notifyDataSetChanged();


                Log.e("onItemClick arg2", " " + arg2);
                Log.e("onItemClick arg3", " " + arg3);


                Protocol protocol = (Protocol) arg0.getItemAtPosition(arg2);

                if (protocol.getErrorCode() != 0) {
                    textError.setText(AutoclaveMonitor.getInstance().getErrorString(protocol.getErrorCode()));
                    textError.setVisibility(View.VISIBLE);
                } else {
                    textError.setText("no error");
                    textError.setVisibility(View.INVISIBLE);
                }

                final MonitorListFragment fragment = MonitorListFragment.newInstance(1, protocol);
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.replace(R.id.content, fragment).commit();

                LineGraph lineGraph = null;
                lineGraph = GraphService.getInstance().getProtocolGraphView(protocol);

                try {
                    graphContainer.addView(lineGraph.getView(getActivity()));
                    progressBarGraph.setVisibility(View.GONE);
                } catch (Exception e) {
                    Log.e("ProtocolsFragment", "exception onPostExecute: " + e.toString());
                }


                if (graphContainer != null)
                    graphContainer.setVisibility(checkBoxGpaphList.isChecked() ? View.GONE : View.VISIBLE);
                if (viewList != null)
                    viewList.setVisibility(checkBoxGpaphList.isChecked() ? View.VISIBLE : View.GONE);

            }
        });


        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        updateProtocolAdapter();
        Intent intent3 = new Intent(getActivity(), PostProtocolsService.class);
        getActivity().startService(intent3);


    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible) {
            spinner.setSelection(0);
            protocolAdapter.notifyDataSetChanged();
        }
    }


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public void onStart() {
        super.onStart();


    }


    private void updateProtocolAdapter() {

        try {

            int position = spinner.getSelectedItemPosition();
            QueryProtoolsTask queryProtocolsTask = new QueryProtoolsTask();
            queryProtocolsTask.execute(position);

        } catch (Exception e) {

        }


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageViewPrint:
                if (!printSelectedProtocol()) {
                    Toast.makeText(getActivity(), getString(R.string.select_protocol_first), Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.imageViewScan:

                askForScan();
                break;
        }
    }

    private void askForScan() {
        try {

            final SweetAlertDialog dialog = new SweetAlertDialog(getActivity(), R.layout.dialog_scan, SweetAlertDialog.WARNING_TYPE);
            dialog.setContentView(R.layout.dialog_scan);
            dialog.setTitle(R.string.scan_item);
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);

            // set the custom dialog components - text, image and button
            final EditText editText = (EditText) dialog.findViewById(R.id.edittext);
            editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {

                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        editText.requestFocus();
                    } else {
                        hideKeyboard(getActivity());
                    }

                }
            });
            editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {


                    String[] barcodeSplitted = editText.getText().toString().split("\\.");
                    Log.e("MenuMain", "length of splitted barcode" + barcodeSplitted.length);
                    if (barcodeSplitted.length == 2) {
                        select(
                                barcodeSplitted[0],
                                barcodeSplitted[1]);
                    } else {
                        Log.e("MenuMain", "invalid barcode" + editText.getText().toString());
                        Toast.makeText(getActivity(), getString(R.string.invalid_barcode), Toast.LENGTH_LONG).show();
                    }
                    dialog.dismissWithAnimation();

                    return true;
                }
            });
            Button dialogButtonNo1 = (Button) dialog.findViewById(R.id.dialogButtonNO);
            dialogButtonNo1.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    dialog.dismissWithAnimation();
                }
            });

            dialog.show();
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();

        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public class QueryProtoolsTask extends AsyncTask<Integer, Integer, List<Protocol>> {

        @Override
        protected List<Protocol> doInBackground(Integer... params) {

            try {
                int orderBy = SPINNER_POSITION_ORDER_BY_START_TIME;
                if (params != null) {
                    if (params.length > 0) {
                        orderBy = params[0];
                    }
                }
                List<Protocol> protocols = null;
                switch (orderBy) {
                    case SPINNER_POSITION_ORDER_BY_START_TIME:
                        protocols = (ArrayList<Protocol>) databaseService.getProtocolsForCurrentUserOrderedByStartTime(false);
                        break;
                    case SPINNER_POSITION_ORDER_BY_PROGRAM_NAME:
                        protocols = (ArrayList<Protocol>) databaseService.getProtocolsOfCurrentUserSortedByProgramName(true);
                        break;
                    case SPINNER_POSITION_ORDER_BY_SUCCESS:
                        protocols = (ArrayList<Protocol>) databaseService.getProtocolsForCurrentUserOrderedBySuccess(true);
                        break;
                    default:
                        protocols = (ArrayList<Protocol>) databaseService.getProtocolsForCurrentUserOrderedByStartTime(false);
                        break;
                }

                return protocols;
            } catch (Exception e) {
                return null;
            }
        }


        @Override
        protected void onPreExecute() {
            progressBarProtocolList.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }


        @Override
        protected void onPostExecute(List<Protocol> result) {
            if (result != null) {
                protocolAdapter.clear();
                for (Protocol protocol : result) {
                    protocolAdapter.add(protocol);
                }

                protocolAdapter.notifyDataSetChanged();
            }
            progressBarProtocolList.setVisibility(View.GONE);
            super.onPostExecute(result);
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);
        }


    }

    public void showLabelPrinterDialog() {
        try {

            final SweetAlertDialog dialog = new SweetAlertDialog(getActivity(), R.layout.dialog_edit_exp_time, SweetAlertDialog.WARNING_TYPE);
            dialog.setContentView(R.layout.dialog_edit_exp_time);
            dialog.setTitle(R.string.enter_time_until_expiration);
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
            // set the custom dialog components - text, image and button

            final EditText editMonths = (EditText) dialog.findViewById(R.id.dialog_edit_number_months);

            Button buttonOk = (Button) dialog.findViewById(R.id.dialog_edit_number_button_ok);
            buttonOk.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    Long months = 0L;


                    try {
                        months = Long.parseLong(editMonths.getText().toString());
                    } catch (Exception e) {
                        months = 0L;
                    }
                    Log.e("ProtocolsFragment", "endtime months in millisek: " + months);
                    try {
                        if (aktPosition >= 0) {
                            Date dateExpire = protocolAdapter.getItem(aktPosition).getEndTime();
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(dateExpire);
                            cal.add(Calendar.MONTH, months.intValue()); //minus number would decrement the days
                            dateExpire = cal.getTime();


                            LabelPrinterUtils.printLabel(dateExpire,
                                    Autoclave.getInstance().getController().getSerialnumber(),
                                    protocolAdapter.getItem(aktPosition).getZyklusNumber(),
                                    1,
                                    protocolAdapter.getItem(aktPosition).getErrorCode() == 0);
                        }
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), getString(R.string.unable_to_print_the_label), Toast.LENGTH_LONG).show();
                    }

                    dialog.dismissWithAnimation();
                }
            });


            Button buttonCancel = (Button) dialog.findViewById(R.id.dialog_edit_number_button_cancel);
            buttonCancel.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    dialog.dismissWithAnimation();

                }
            });
            Button buttonSkip = (Button) dialog.findViewById(R.id.dialog_edit_number_button_skip);
            buttonSkip.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    try {
                        if (aktPosition >= 0) {
                            Date dateExpire = protocolAdapter.getItem(aktPosition).getEndTime();
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(dateExpire);
                            cal.add(Calendar.MONTH, 6); //minus number would decrement the days
                            dateExpire = cal.getTime();

                            LabelPrinterUtils.printLabel(dateExpire,
                                    Autoclave.getInstance().getController().getSerialnumber(),
                                    protocolAdapter.getItem(aktPosition).getZyklusNumber(),
                                    1,
                                    protocolAdapter.getItem(aktPosition).getErrorCode() == 0);
                        }
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), "Unable to print the label", Toast.LENGTH_LONG).show();
                    }
                    dialog.dismissWithAnimation();

                }
            });

            dialog.show();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean printSelectedProtocol() {

        try {
            if (protocolAdapter.getItem(aktPosition) == null) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        try {


            final SweetAlertDialog dialog = new SweetAlertDialog(getActivity(), R.layout.dialog_protocol_label, SweetAlertDialog.WARNING_TYPE);
            dialog.setContentView(R.layout.dialog_protocol_label);
            dialog.setTitle(R.string.please_choose_one_of_the_following_options);
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);


            Button buttonLabel = (Button) dialog.findViewById(R.id.dialogButtonLabel);
            buttonLabel.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    showLabelPrinterDialog();

                    dialog.dismissWithAnimation();
                }
            });


            Button buttonProtocol = (Button) dialog.findViewById(R.id.dialogButtonProtocol);
            buttonProtocol.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Helper.printProtocols(getActivity(), protocolAdapter.getItem(aktPosition), new MyCallback() {
                        SweetAlertDialog dialogLocal;

                        @Override
                        public void onSuccess(Object response, int requestId) {
                            dialogLocal.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                            dialogLocal.setTitleText(getString(R.string.protocol_printed));
                        }

                        @Override
                        public void onError(ErrorModel error, int requestId) {
                            dialogLocal.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                            dialogLocal.setTitleText(getString(R.string.please_select_a_protocol_first));
                        }

                        @Override
                        public void onStart(int requestId) {
                            dialog.dismiss();
                            dialogLocal = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
                            dialogLocal.setCancelable(false);
                            dialogLocal.setTitleText(getString(R.string.sending));
                            dialogLocal.show();
                        }

                        @Override
                        public void onProgress(int current, int max) {

                        }
                    });

                }
            });

            Button buttonCancel = (Button) dialog.findViewById(R.id.dialogButtonCancel);
            buttonCancel.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    dialog.dismissWithAnimation();

                }
            });

            dialog.show();


        } catch (Exception e) {
            return false;

        }

        return true;


    }


    public void select(String Serialnum, String cyclenumber) {
        boolean isItemFound = false;
        try {
            for (int i = 0; i < protocolAdapter.getCount(); i++) {
                if (Autoclave.getInstance().getController().getSerialnumber().equals(Serialnum)) {
                    if (protocolAdapter.getItem(i).getZyklusNumber() == Integer.parseInt(cyclenumber)) {

                        list.setSelection(i);
                        list.performItemClick(
                                list.getChildAt(i),//.getView(i, null, null),
                                i,
                                list.getAdapter().getItemId(i));
                        isItemFound = true;
                        break;
                    }
                } else {
                    Toast.makeText(getActivity(), getString(R.string.s_n_doesn_t_match_protocol_from_other_autoclave), Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Log.e("ProtocolsFragment", e.toString());
        }
        if (isItemFound == false) {
            Toast.makeText(getActivity(), "Item not found", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getActivity(), "Item found", Toast.LENGTH_LONG).show();
        }

    }

}

