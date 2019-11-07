package com.certoclav.library.certocloud;

import android.os.AsyncTask;

import com.certoclav.library.util.Response;

import org.json.JSONObject;

public class NotificationService {


    public void executePostSmsTask(final String phoneNumber, final String message) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    for (int i = 0; i < 5; i++) {
                        Response response = sendSms(phoneNumber, message);
                        if (response != null && response.isOK()) {
                            break;
                        }
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();


    }


    public void executePostEmailTask(final String emailAddressReceiver, final String nameOfReceiver, final String subjectOfMail, final String contentOfMail) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                for (int i = 0; i < 5; i++) {
                    Response response = sendEmail(emailAddressReceiver, nameOfReceiver, subjectOfMail, contentOfMail);
                    if (response != null && response.isOK()) {
                        break;
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                    }
                }
                return null;
            }
        }.execute();


    }


    private Response sendSms(String phoneNumber, String message) {

        PostUtil postUtil = new PostUtil();

        JSONObject body = new JSONObject();
        try {
            body.put("phone", phoneNumber);
            body.put("body", message);
        } catch (Exception e) {

        }

        return postUtil.postToCertocloud(body.toString(), CertocloudConstants.getServerUrl() + CertocloudConstants.REST_API_POST_SMS, true);
    }


    private Response sendEmail(String emailAddressReceiver, String nameOfReceiver, String subjectOfMail, String contentOfMail) {

        PostUtil postUtil = new PostUtil();

        JSONObject body = new JSONObject();
        try {
            body.put("to", emailAddressReceiver); //to email
            body.put("username", nameOfReceiver);//name of receiver
            body.put("subject", subjectOfMail); //subject of mail
            body.put("title", subjectOfMail); //title of mail
            body.put("description", contentOfMail); //content of mail
        } catch (Exception e) {

        }

        return postUtil.postToCertocloud(body.toString(), CertocloudConstants.getServerUrl() + CertocloudConstants.REST_API_POST_EMAIL, true);
    }


}
