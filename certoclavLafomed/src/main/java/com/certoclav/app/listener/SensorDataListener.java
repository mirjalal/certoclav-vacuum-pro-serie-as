package com.certoclav.app.listener;

import com.certoclav.app.model.AutoclaveData;

public interface SensorDataListener {
 void onSensorDataChange(AutoclaveData data);
}
