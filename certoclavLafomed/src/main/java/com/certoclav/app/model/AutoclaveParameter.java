package com.certoclav.app.model;

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
        return value;
    }

    public void setParameterId(int parameterId) {
        this.parameterId = parameterId;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
