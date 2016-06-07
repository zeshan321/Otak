package utils;

import callbacks.NetworkChangeCallback;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkHandler {

    private String url;
    private int timeout = 5000;
    private int sleepTime = 1000;
    private boolean previousRequest = false;
    private NetworkChangeCallback.NetworkType networkType = NetworkChangeCallback.NetworkType.ONLINE;

    public NetworkHandler(String url) {
        this.url = url.replaceFirst("^https", "http");
    }

    public void run(NetworkChangeCallback callback) {
        // Check server status
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (!previousRequest) {
                        previousRequest = true;

                        try {
                            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                            connection.setConnectTimeout(timeout);
                            connection.setReadTimeout(timeout);
                            connection.setRequestMethod("GET");
                            connection.setRequestProperty("Accept-Encoding", "gzip");

                            int responseCode = connection.getResponseCode();
                            if (200 <= responseCode && responseCode <= 399) {
                                if (!networkType.equals(NetworkChangeCallback.NetworkType.ONLINE)) {
                                    networkType = NetworkChangeCallback.NetworkType.ONLINE;

                                    callback.onNetworkChange(networkType);
                                }
                            } else {
                                if (!networkType.equals(NetworkChangeCallback.NetworkType.UNKNOWN)) {
                                    networkType = NetworkChangeCallback.NetworkType.UNKNOWN;

                                    callback.onNetworkChange(networkType);
                                }
                            }

                            previousRequest = false;
                        } catch (IOException exception) {
                            if (!networkType.equals(NetworkChangeCallback.NetworkType.OFFLINE)) {
                                networkType = NetworkChangeCallback.NetworkType.OFFLINE;

                                callback.onNetworkChange(networkType);
                            }

                            previousRequest = false;
                        }
                    }
                }
            }
        }.start();
    }
}
