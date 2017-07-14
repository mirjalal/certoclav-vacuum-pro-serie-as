package com.certoclav.app.responsemodels;

import com.google.gson.annotations.SerializedName;

/**
 * Created by musaq on 2/13/2017.
 */

public class ResponseModel {
    @SerializedName("error")
    private boolean isOk;
    private String message;

    public String getMessage() {
        return message;
    }

    public boolean isOk() {
        return !isOk;
    }
}
