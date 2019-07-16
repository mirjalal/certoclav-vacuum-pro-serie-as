package com.certoclav.app.util;

import android.util.Pair;

import com.certoclav.app.AppConstants;
import com.certoclav.app.model.AutoclaveParameter;
import com.certoclav.app.model.ErrorModel;
import com.certoclav.app.service.ReadAndParseSerialService;

import java.util.Arrays;
import java.util.List;

/**
 * Created by musaq on 10/29/2018.
 */

public class AutoclaveModelManager implements MyCallback {

    private static AutoclaveModelManager manager;
    private Integer[] parametersForAdmin = new Integer[]{1, 2, 3, 4, 71, 72, 94, 95};
    private Integer[] parametersTemperature = new Integer[]{36, 37, 39, 40};
    private String[] parametersSkipForAuditLog = new String[]{"preferences_autoclave_parameter_94",
            "preferences_autoclave_parameter_95", "preferences_autoclave_parameter_96",
            "preferences_autoclave_parameter_98", AppConstants.PREFERENCE_KEY_ID_OF_LAST_USER};
    private int currentSentParameterId = 1;

    private AutoclaveModelManager() {
        try {
            ReadAndParseSerialService.getInstance().addCallback(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static AutoclaveModelManager getInstance() {
        if (manager == null)
            manager = new AutoclaveModelManager();
        return manager;
    }


    private AutoclaveParameter model;
    private AutoclaveParameter serialNumber;
    private AutoclaveParameter pcbSerialNumber;
    private AutoclaveParameter temperatureSymbol;

    public String getSerialNumber() {
        if (serialNumber == null) {
            return null;
        }
        return serialNumber.getValue().toString();
    }

    public String getPCBSerialNumber() {
        if (pcbSerialNumber == null) {
            return null;
        }
        return pcbSerialNumber.getValue().toString();
    }

    public String getTemperatureUnit() {
        return temperatureSymbol != null ? temperatureSymbol.getValue().toString() : "C";
    }

    public boolean isFahrenheit() {
        return getTemperatureUnit().equalsIgnoreCase("F");
    }

    public void setTemperatureSymbol(AutoclaveParameter temperatureSymbol) {
        this.temperatureSymbol = temperatureSymbol;
    }

    public void setSerialNumber(AutoclaveParameter serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getModel() {
        if (model == null)
            return null;
        return model.getValue().toString();
    }

    public Pair<Float, Float> getSterilizationTempRange() {
        switch (model.getValue().toString()) {
            case "AEB":
            case "AHSB":
                return new Pair<>(Helper.getInstance().celsiusToCurrentUnit(100f),
                        Helper.getInstance().celsiusToCurrentUnit(134f));
            default:
                return new Pair<>(Helper.getInstance().celsiusToCurrentUnit(100f),
                        Helper.getInstance().celsiusToCurrentUnit(140f));
        }
    }


    public Pair<Float, Float> getWarmingUpTempRange() {
        return new Pair<>(Helper.getInstance().celsiusToCurrentUnit(0f),
                Helper.getInstance().celsiusToCurrentUnit(199f));
    }

    public Pair<Float, Float> getFinalTempRange() {
        return new Pair<>(Helper.getInstance().celsiusToCurrentUnit(50f),
                Helper.getInstance().celsiusToCurrentUnit(95f));
    }

    public Pair<Float, Float> getZValueTempRange() {
        return new Pair<>(Helper.getInstance().celsiusToCurrentUnit(0.1f),
                Helper.getInstance().celsiusToCurrentUnit(100f));
    }

    public Pair<Float, Float> getOffsetTempRange() {
        return new Pair<>(Helper.getInstance().celsiusToCurrentUnit(-10),
                Helper.getInstance().celsiusToCurrentUnit(10f));
    }

    public Pair<Integer, Integer> getSterilizationTimeRange() {
        switch (model.getValue().toString()) {
            case "AEB":
            case "AHSB":
                return new Pair<>(1, 250);
            default:
                return new Pair<>(1, 360);
        }
    }

    public boolean isDryTimeExists() {
        return Arrays.asList(new String[]{"AEB", "AHSB", "TLVPD"}).contains(getModelName());
    }

    public Pair<Integer, Integer> getVacuumPulseRange() {
        switch (getModelName()) {
            case "AEB":
            case "AHSB":
            case "TLVPD":
                return new Pair<>(1, 3);
            case "TLVFA":
            case "TLV":
                return new Pair<>(0, 1);
        }
        //No Vacuum Phase exists
        return null;
    }

    public Pair<Float, Float> getParamterRange(int paramterId) {
        switch (paramterId) {
            case 3:
                return new Pair<>(0f, 99999f);
            case 27:
                return new Pair<>(50f, 95f);
            case 42:
                return new Pair<>(0f, 2f);
            case 98:
                return new Pair<>(0f, 100f);
            case 40:
                if (getModelName().contains("TLV"))
                    return new Pair<>(Helper.getInstance().celsiusToCurrentUnit(100), Helper.getInstance().celsiusToCurrentUnit(140));
                return new Pair<>(Helper.getInstance().celsiusToCurrentUnit(0), Helper.getInstance().celsiusToCurrentUnit(140));
            case 99:
                switch (getModelName()) {
                    case "TLVPD":
                        return new Pair<>(20f, 90f);
                    default:
                        return new Pair<>(30f, 80f);
                }
        }
        //No Range
        return null;
    }

    //Here, for PD model, when the autoclave starts, the first stage is warning-up which doesn't
    // allow to users to start a program during the stage
    public boolean isWarmingUpEnabled() {
        switch (getModelName()) {
            case "AHSB":
            case "AEB":
            case "TLVPD":
                return true;
            case "TLVFA":
            case "TLV":
        }
        //No Vacuum Phase exists
        return false;
    }

    public boolean hasTwoFlexProbe2() {
        return getModelName().contains("TLV");
    }


    public List<Integer> getAdminParameters() {
        return Arrays.asList(parametersForAdmin);
    }


    public List<String> getParametersSkipForAuditLog() {
        return Arrays.asList(parametersSkipForAuditLog);
    }

    public boolean isF0Exists() {
        return Arrays.asList(new String[]{"TLVPD", "TLVFA", "TLV"}).contains(getModelName());
    }

    public boolean isMaintaingingTempExistsInProgram() {
        return Arrays.asList(new String[]{"TLVFA", "TLV"}).contains(getModelName());
    }

    public boolean isMaintaingingTempExistsInParameters() {
        return Arrays.asList(new String[]{"AEB", "AHSB", "TLVPD"}).contains(getModelName());
    }

    public boolean isFinalTempExistsInProgramEdit() {
        return Arrays.asList(new String[]{"TLV", "TLVFA"}).contains(getModelName());
    }

    public boolean isWarmUpTempExistsInParameters() {
        return Arrays.asList(new String[]{"AEB", "AHSB", "TLVPD"}).contains(getModelName());
    }

    public boolean isCoolingParameterExists() {
        return Arrays.asList(new String[]{"TLVFA"}).contains(getModelName());
    }

    private String getModelName() {
        try {
            return model.getValue().toString().toUpperCase().replaceAll("\\d", "").replaceAll("-", "");
        } catch (Exception e) {
            return "NA";
        }
    }

    public void setModel(AutoclaveParameter model) {
        this.model = model;
    }

    @Override
    public void onSuccess(Object response, int requestId) {
        if (requestId == ReadAndParseSerialService.HANDLER_MSG_ACK_GET_PARAMETER) {
            if (((AutoclaveParameter) response).getParameterId() == 1) {
                model = (AutoclaveParameter) response;
                ReadAndParseSerialService.getInstance().getParameter(3);
            }
            if (((AutoclaveParameter) response).getParameterId() == 3) {
                serialNumber = (AutoclaveParameter) response;
                ReadAndParseSerialService.getInstance().getParameter(4);
            }

            if (((AutoclaveParameter) response).getParameterId() == 4) {
                pcbSerialNumber = (AutoclaveParameter) response;
                ReadAndParseSerialService.getInstance().getParameter(8);
            }

            if (((AutoclaveParameter) response).getParameterId() == 8) {
                temperatureSymbol = (AutoclaveParameter) response;
            }
        }
    }

    @Override
    public void onError(ErrorModel error, int requestId) {
    }

    @Override
    public void onStart(int requestId) {

    }

    @Override
    public void onProgress(int current, int max) {

    }

    public boolean isTemperatureParameter(int id) {
        return Arrays.asList(parametersTemperature).contains(id);
    }

    public boolean showGenPress() {
        return Arrays.asList(new String[]{"TLVPD"}).contains(getModelName());
    }

    public int getVacuumProgramIndex() {
        return 2;
    }
}
