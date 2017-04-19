package com.certoclav.app.listener;

import com.certoclav.app.model.AutoclaveState;



public interface AutoclaveStateListener {
 void onAutoclaveStateChange(AutoclaveState state);
}
