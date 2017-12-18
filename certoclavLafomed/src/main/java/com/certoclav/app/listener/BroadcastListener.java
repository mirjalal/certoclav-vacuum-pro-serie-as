package com.certoclav.app.listener;

import org.json.JSONObject;

/**
 * Created by musaq on 9/30/2017.
 */

public interface BroadcastListener {
    void onReceived(JSONObject data);
    void onFailed();
    void onTimeout();
}
