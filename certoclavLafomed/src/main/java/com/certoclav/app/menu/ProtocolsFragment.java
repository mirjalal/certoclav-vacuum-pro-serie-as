package com.certoclav.app.menu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.app.adapters.ProtocolAdapter;
import com.certoclav.app.database.DatabaseService;
import com.certoclav.app.database.Protocol;
import com.certoclav.app.graph.GraphService;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveMonitor;
import com.certoclav.app.model.ErrorModel;
import com.certoclav.app.monitor.CertoTraceListFragment;
import com.certoclav.app.monitor.MonitorListFragment;
import com.certoclav.app.service.PostProtocolsService;
import com.certoclav.app.util.Helper;
import com.certoclav.app.util.LabelPrinterUtils;
import com.certoclav.app.util.MyCallback;
import com.certoclav.app.util.ServerConfigs;
import com.certoclav.library.application.ApplicationController;
import com.certoclav.library.graph.LineGraph;
import com.paging.listview.PagingListView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ProtocolsFragment extends Fragment implements View.OnClickListener {


    private static final long MAX_PROTOCOLS_IN_EACH_PAGE = 10;
    private PagingListView list;
    private ProtocolAdapter protocolAdapter;
    private DatabaseService databaseService;
    private TextView textError;
    private Spinner spinner;
    private ArrayAdapter<String> dataAdapter;
    private ProgressBar progressBarProtocolList;
    private ProgressBar progressBarGraph;
    private ImageView checkBoxGpaphList;
    private View viewListTrace;
    private View viewList;

    private SharedPreferences sharedPreferences;
    public static final int SPINNER_POSITION_ORDER_BY_START_TIME = 0;
    public static final int SPINNER_POSITION_ORDER_BY_PROGRAM_NAME = 1;


    public static final int SPINNER_POSITION_ORDER_BY_SUCCESS = 2;
    int aktPosition = 0;
    private LinearLayout graphContainer;
    private SweetAlertDialog pDialog;
    private View buttonDownload;
    private int graphListTraceButtonState = 0;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.menu_fragment_protocols, container, false); //je nach mIten könnte man hier anderen Inhalt laden.
        sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        checkBoxGpaphList = (ImageView) rootView.findViewById(R.id.checkboxGraphListTrace);
        updateGraphListTraceButton();
        checkBoxGpaphList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                graphListTraceButtonState++;
                graphListTraceButtonState %= 3;
                sharedPreferences.edit().putInt(AppConstants.PREFERENCE_KEY_LIST_GRAPH, graphListTraceButtonState).commit();
                updateGraphListTraceButton();
            }
        });

        textError = (TextView) rootView.findViewById(R.id.protocols_text_error);
        textError.setVisibility(View.INVISIBLE);
        progressBarProtocolList = (ProgressBar) rootView.findViewById(R.id.protocols_progress_bar_list);
        progressBarGraph = (ProgressBar) rootView.findViewById(R.id.protocols_progress_bar_graph);
        databaseService = DatabaseService.getInstance();

        rootView.findViewById(R.id.imageViewPrint).setOnClickListener(this);
        buttonDownload = rootView.findViewById(R.id.imageViewDownloadProtocol);
        buttonDownload.setOnClickListener(this);
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
                // 
            }
        });


        //fill protocoladapter with an empty list. The list will be filled in a thread later
        protocolAdapter = new ProtocolAdapter(getActivity().getApplicationContext(), new ArrayList<Protocol>());
        list = (PagingListView) rootView.findViewById(R.id.protocols_list);
        // list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        list.setCacheColorHint(0);
        list.setItemsCanFocus(true);
        list.setAdapter(protocolAdapter);

        list.setOnItemClickListener(new OnItemClickListener() {


            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int pos, long arg3) {
                aktPosition = pos;
                selectProtocol(pos);
            }
        });


        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        updateProtocolAdapter();
    }


    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
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
        protocolAdapter.setCurrentPage(0);
        protocolAdapter.clear();
        list.setHasMoreItems(true);
        list.setPagingableListener(new PagingListView.Pagingable() {
            @Override
            public void onLoadMoreItems() {
                int position = spinner.getSelectedItemPosition();
                QueryProtoolsTask queryProtocolsTask = new QueryProtoolsTask();
                queryProtocolsTask.execute(position, protocolAdapter.getCurrentPage() + 1);
            }
        });
    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.imageViewPrint:
                if (!printSelectedProtocol()) {
                    Toast.makeText(getActivity(), getString(R.string.select_protocol_first), Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.imageViewScan:
                askForScan();
                break;
            case R.id.imageViewDownloadProtocol:
                if ((ApplicationController.getInstance().isNetworkAvailable() || ServerConfigs.getInstance(getActivity()).getUrl() != null) && protocolAdapter.getCount() > aktPosition && protocolAdapter.getItem(aktPosition) != null) {
                    view.setEnabled(false);
                    final SweetAlertDialog barProgressDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
                    barProgressDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                    barProgressDialog.setTitleText(getActivity().getString(com.certoclav.library.R.string.downloading));
                    barProgressDialog.setCancelable(false);
                    Helper.downloadProtocol(getActivity(), protocolAdapter.getItem(aktPosition), new MyCallback() {
                        @Override
                        public void onSuccess(Object response, int requestId) {
                            if ((Boolean) response)
                                if (requestId == 1)
                                    barProgressDialog.setTitleText(getActivity().getString(R.string.adding));
                                else {
                                    protocolAdapter.updateProtocol(aktPosition, databaseService.getProtocolByCloudId(protocolAdapter.getItem(aktPosition).getCloudId()));
                                    selectProtocol(aktPosition);
                                    barProgressDialog.setTitleText(getActivity().getString(R.string.download_success));
                                    barProgressDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                    view.setEnabled(true);
                                    //  updateProtocolAdapter();
                                }
                            else {
                                barProgressDialog.setTitleText(getActivity().getString(R.string.something_went_wrong_try_again));
                                barProgressDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                            }

                        }

                        @Override
                        public void onError(ErrorModel error, int requestId) {
                            barProgressDialog.setTitleText(error.getMessage() != null ? error.getMessage() : getActivity().getString(R.string.download_failed));
                            barProgressDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                            view.setEnabled(true);
                        }

                        @Override
                        public void onStart(int requestId) {
                            barProgressDialog.show();
                        }

                        public void onProgress(int current, int max) {
                            barProgressDialog.setTitleText(getActivity().getString(R.string.adding) + " (" + current + " / " + max + ")");
                        }
                    });
                } else {

                    SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                            .setTitleText(getString(R.string.enable_network_communication))
                            .setConfirmText(getString(R.string.ok))
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();
                                }
                            }).setCustomImage(R.drawable.ic_network_connection);
                    sweetAlertDialog.setCanceledOnTouchOutside(true);
                    sweetAlertDialog.setCancelable(true);
                    sweetAlertDialog.show();
                }

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
            dialogButtonNo1.setOnClickListener(new View.OnClickListener() {

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
                long page = params[1];
                long limit = MAX_PROTOCOLS_IN_EACH_PAGE;
                switch (orderBy) {
                    case SPINNER_POSITION_ORDER_BY_START_TIME:
                        return databaseService.getProtocols(page, limit, Protocol.FIELD_PROTOCOL_START_TIME, false);
                    case SPINNER_POSITION_ORDER_BY_PROGRAM_NAME:
                        return databaseService.getProtocols(page, limit, Protocol.FIELD_PROGRAM_NAME, true);
                    case SPINNER_POSITION_ORDER_BY_SUCCESS:
                        return databaseService.getProtocols(page, limit, Protocol.FIELD_PROGRAM_STATUS, true);
                    default:
                        return databaseService.getProtocols(page, limit, Protocol.FIELD_PROTOCOL_START_TIME, false);
                }

            } catch (Exception e) {
                return null;
            }
        }


        @Override
        protected void onPostExecute(List<Protocol> result) {
            if (ProtocolsFragment.this.isVisible())
                if (result != null) {
                    protocolAdapter.setCurrentPage(protocolAdapter.getCurrentPage() + 1);
                    for (Protocol protocol : result) {
                        protocolAdapter.add(protocol);
                    }

                    protocolAdapter.notifyDataSetChanged();
                    list.onFinishLoading(result.size() > 0, result);
                    if (protocolAdapter.getCount() > aktPosition)
                        list.performItemClick(
                                list.getChildAt(aktPosition),//.getView(i, null, null),
                                aktPosition,
                                list.getAdapter().getItemId(0));
                } else {
                    list.onFinishLoading(false, null);
                }
            super.onPostExecute(result);
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            // 
            super.onProgressUpdate(values);
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
            buttonLabel.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    showLabelPrinterDialog();
                    dialog.dismissWithAnimation();
                }
            });


            Button buttonProtocol = (Button) dialog.findViewById(R.id.dialogButtonProtocol);
            buttonProtocol.setOnClickListener(new View.OnClickListener() {

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
            buttonCancel.setOnClickListener(new View.OnClickListener() {

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
            buttonOk.setOnClickListener(new View.OnClickListener() {

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
            buttonCancel.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dialog.dismissWithAnimation();

                }
            });
            Button buttonSkip = (Button) dialog.findViewById(R.id.dialog_edit_number_button_skip);
            buttonSkip.setOnClickListener(new View.OnClickListener() {

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

    private void select(String Serialnum, String cyclenumber) {
        boolean isItemFound = false;
        try {
            for (int i = 0; i < protocolAdapter.getCount(); i++) {
                if (Autoclave.getInstance().getController().getSerialnumber().equals(Serialnum)) {
                    if (protocolAdapter.getItem(i).getZyklusNumber() == Integer.parseInt(cyclenumber)) {

                        list.setSelection(i);
                        final int finalI = i;
                        list.smoothScrollToPosition(i);
                        list.post(new Runnable() {
                            @Override
                            public void run() {
                                list.smoothScrollToPosition(finalI);
                                selectProtocol(finalI);
                            }
                        });

                        isItemFound = true;
                        break;
                    }
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


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && Autoclave.getInstance().isOnlineMode(getContext())) {
            Intent intent5 = new Intent(ApplicationController.getContext(), PostProtocolsService.class);
            getActivity().startService(intent5);
        } else {
        }
    }


    private void selectProtocol(int pos) {
        graphContainer = (LinearLayout) getActivity().findViewById(R.id.protocols_container_graph);
        viewList = getActivity().findViewById(R.id.content);
        viewListTrace = getActivity().findViewById(R.id.certoclavTrace);
        graphContainer.removeAllViews();

        final LinearLayout graphContainer = (LinearLayout) getActivity().findViewById(R.id.protocols_container_graph);
        graphContainer.removeAllViews();
        progressBarGraph.setVisibility(View.VISIBLE);
        protocolAdapter.setSelection(pos);
        protocolAdapter.notifyDataSetChanged();

        Protocol protocol = protocolAdapter.getItem(pos);
        buttonDownload.setVisibility(protocol == null || !protocol.isUploaded()
                || (protocol.getProtocolEntry() != null && protocol.getProtocolEntry().size() > 0)
                || !Autoclave.getInstance().isOnlineMode(getActivity())
                ? View.GONE : View.VISIBLE);

        textError.setText(AutoclaveMonitor.getInstance().getErrorString(protocol.getErrorCode()));
        ((View) textError.getParent()).setVisibility(View.VISIBLE);
        textError.setTextColor(getResources().getColor(protocol.getErrorCode() == 0 ? R.color.success_color : R.color.error_color));
        textError.setVisibility(View.VISIBLE);


        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        try {
            transaction.replace(R.id.content, MonitorListFragment.newInstance(1, protocol));
            transaction.replace(R.id.certoclavTrace, CertoTraceListFragment.newInstance(1, protocol)).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        LineGraph lineGraph = null;
        lineGraph = GraphService.getInstance().getProtocolGraphView(protocol);

        try {
            graphContainer.addView(lineGraph.getView(getActivity()));
            progressBarGraph.setVisibility(View.GONE);
        } catch (Exception e) {
            Log.e("ProtocolsFragment", "exception onPostExecute: " + e.toString());
        }
        updateGraphListTraceButton();
    }

    private void updateGraphListTraceButton() {
        switch (graphListTraceButtonState = sharedPreferences.getInt(AppConstants.PREFERENCE_KEY_LIST_GRAPH, 0)) {
            case 0:
                checkBoxGpaphList.setImageResource(R.drawable.bg_list);
                break;
            case 1:
                checkBoxGpaphList.setImageResource(R.drawable.bg_trace);
                break;
            case 2:
                checkBoxGpaphList.setImageResource(R.drawable.bg_graph);
                break;
        }

        if (graphContainer != null) {
            graphContainer.setVisibility(graphListTraceButtonState == 0 ? View.VISIBLE : View.GONE);
        }
        if (viewList != null) {
            viewList.setVisibility(graphListTraceButtonState == 1 ? View.VISIBLE : View.GONE);
        }
        if (viewListTrace != null) {
            viewListTrace.setVisibility(graphListTraceButtonState == 2 ? View.VISIBLE : View.GONE);
        }
    }

}

