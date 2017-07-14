package com.certoclav.app.util;


import com.certoclav.app.model.ErrorModel;

/**
 * Created by musaq on 2/13/2017.
 */

public interface MyCallback {
    void onSuccess(Object response, int requestId);

    void onError(ErrorModel error, int requestId);

    void onStart(int requestId);

    void onProgress(int current, int max);
}
