package com.certoclav.app.util;

import android.util.Pair;

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
    private Integer[] parametersForAdmin = new Integer[]{1, 2, 3, 4, 71, 72, 94, 95, 96, 98};
    private int currentSentParameterId = 1;

    private AutoclaveModelManager() {
        ReadAndParseSerialService.getInstance().addCallback(this);
    }

    public static AutoclaveModelManager getInstance() {
        if (manager == null)
            manager = new AutoclaveModelManager();

        return manager;
    }

    private AutoclaveParameter model;
    private AutoclaveParameter serialNumber;

    public String getSerialNumber() {
        if (serialNumber == null) {
            return null;
        }
        return serialNumber.getValue().toString();
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
                return new Pair<>(100f, 134f);
            default:
                return new Pair<>(100f, 140f);
        }
    }


    public Pair<Float, Float> getWarmingUpTempRange() {
        return new Pair<>(0f, 199f);
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

    //Here, for PD model, when the autoclave starts, the first stage is warning-up which doesn't
    // allow to users to start a program during the stage
    public boolean isWarmingUpEnabled() {
        switch (getModelName()) {
            case "AHSB":
            case "TLVPD":
            case "AEB":
                return true;
            case "TLVFA":
            case "TLV":
        }
        //No Vacuum Phase exists
        return false;
    }

    public List<Integer> getAdminParameters() {
        return Arrays.asList(parametersForAdmin);
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

    public boolean isWarmUpTempExistsInParameters() {
        return Arrays.asList(new String[]{"AEB", "AHSB", "TLVPD"}).contains(getModelName());
    }

    public boolean isCoolingParameterExists() {
        return Arrays.asList(new String[]{"TLVFA"}).contains(getModelName());
    }

    private String getModelName() {
        return model.getValue().toString().toUpperCase().replaceAll("\\d", "").replaceAll("-", "");
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
}
