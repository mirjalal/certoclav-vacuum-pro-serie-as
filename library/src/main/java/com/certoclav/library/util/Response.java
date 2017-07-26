package com.certoclav.library.util;

/**
 * Created by musaq on 5/19/2017.
 */

public class Response {
    private boolean error;
    private int status;
    private String message;

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message != null ? message : "";
    }

    public boolean isOK() {
        return status == 200;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
