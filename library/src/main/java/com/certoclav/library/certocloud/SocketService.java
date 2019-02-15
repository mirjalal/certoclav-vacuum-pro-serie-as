package com.certoclav.library.certocloud;

import android.util.Log;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

// listen => server: give data, devicekey
// emitter => android: new data, devicekey

public class SocketService {

    public static final String EVENT_SEND_DATA_FROM_ANDROID_TO_SERVER = "android:give";
    public static final String EVENT_REGISTER = "register";
    public static final String EVENT_START_SEND = "server:get";
    public static final String EVENT_PROGRAM_EDIT = "server:get:edit";
    public static final String EVENT_GET_LIVE_DEBUG = "server:get:livedebug";
    public static final String EVENT_SEND_LIVE_DEBUG = "android:give:livedebug";

    private static SocketService instance;

    private String deviceKey = "";


    public static synchronized SocketService getInstance() {
        if (instance != null)
            return instance;
        return instance = new SocketService();
    }

    public interface SocketEventListener {
        void onSocketEvent(String eventIdentifier, Object... args);
    }

    private SocketEventListener listener = null;


    public void setOnSocketEventListener(SocketEventListener listener) {
        this.listener = listener;
    }

    private Socket socket = null;

    public Socket getSocket() {
        return socket;
    }

    public void connectToCertocloud() {
        socket.connect();
    }

    private SocketService() {


        IO.Options opts = new IO.Options();
        opts.forceNew = true; //do not use old cached sockets
        opts.reconnection = true;
        opts.query = "X-Access-Token=" + CloudUser.getInstance().getToken();

        try {

            socket = IO.socket(CertocloudConstants.getServerUrl(), opts);
            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    if (listener != null)
                        listener.onSocketEvent(Socket.EVENT_CONNECT, args);
                    Log.e("SocketService", "SOCKET CONNECTED");
                }

            }).on(Socket.EVENT_CONNECTING, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    if (listener != null)
                        listener.onSocketEvent(Socket.EVENT_CONNECTING, args);
                    Log.e("SocketService", "SOCKET CONNECTING");

                }

            }).on(EVENT_START_SEND, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    if (listener != null)
                        listener.onSocketEvent(EVENT_START_SEND, args);
                    Log.e("SocketService", "SOCKET EVENT_START_SEND");
                }
            })
                    .on(EVENT_PROGRAM_EDIT, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            if (listener != null)
                                listener.onSocketEvent(EVENT_PROGRAM_EDIT, args);
                            Log.e("SocketService", "SOCKET EVENT_START_SEND");
                        }
                    })
                    .on(EVENT_GET_LIVE_DEBUG, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            if (listener != null)
                                listener.onSocketEvent(EVENT_GET_LIVE_DEBUG, args);
                            Log.e("SocketService", "SOCKET EVENT_START_SEND");
                        }
                    }).
                    on(Socket.EVENT_MESSAGE, new Emitter.Listener() {

                        @Override
                        public void call(Object... args) {
                            if (listener != null)
                                listener.onSocketEvent(Socket.EVENT_MESSAGE, args);
                            Log.e("SocketService", "SOCKET EVENT_MESSAGE");

                        }

                    }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    if (listener != null)
                        listener.onSocketEvent(Socket.EVENT_CONNECT_ERROR, args);
                    Log.e("SocketService", "SOCKET EVENT_CONNECT_ERROR");

                }

            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    if (listener != null)
                        listener.onSocketEvent(Socket.EVENT_DISCONNECT, args);
                    Log.e("SocketService", "SOCKET EVENT_disconnect");
                }

            });


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public String getDeviceKey() {
        return deviceKey;
    }

    public void setDeviceKey(String deviceKey) {
        this.deviceKey = deviceKey;
    }

    public void endService() {
        if (socket != null)
            socket.close();
        instance = null;
    }


}
