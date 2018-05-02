package com.certoclav.app.model;

/**
 * Created by musaq on 7/3/2017.
 */

public class ErrorModel {
    private int statusCode;
    private String message;

    public ErrorModel() {

    }

    public ErrorModel(String message) {
        this.message = message;
    }

    public ErrorModel(String message, int statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
