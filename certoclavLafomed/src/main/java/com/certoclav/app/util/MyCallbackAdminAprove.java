package com.certoclav.app.util;


/**
 * Created by musaq on 2/13/2017.
 */

public interface MyCallbackAdminAprove {
    int APPROVED = 1, DENIED = 0, CLOSED = 2;

    void onResponse(int requestId, int responseId);
}
