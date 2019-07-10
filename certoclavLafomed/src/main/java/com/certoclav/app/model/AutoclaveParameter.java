package com.certoclav.app.model;

import com.certoclav.app.util.AutoclaveModelManager;
import com.certoclav.app.util.Helper;

public class AutoclaveParameter {
    private int parameterId;
    private Object value;

    public AutoclaveParameter(int parameterId, Object value) {
        this.parameterId = parameterId;
        this.value = value;
    }

    public int getParameterId() {
        return parameterId;
    }

    public Object getValue() {
        if (AutoclaveModelManager.getInstance().isTemperatureParameter(parameterId))
            return Helper.getInstance().celsiusToCurrentUnit(Float.valueOf(value.toString()));
        return value;
    }

    public void setParameterId(int parameterId) {
        this.parameterId = parameterId;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
