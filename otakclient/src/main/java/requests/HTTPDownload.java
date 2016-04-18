package requests;

import callback.DownloadCallback;
import org.apache.commons.io.IOUtils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;

public class HTTPDownload {

    private String url;

    public HTTPDownload(String url, String params) {
        this.url = url + params;
    }

    public void downloadFile(final File file, final DownloadCallback callBack) {
        new Thread() {
            @Override
            public void run() {
                try {
                    // Check if to use https or not
                    if (url.startsWith("HTTPS://") || url.startsWith("https://")) {
                        URL urlObj = new URL(url);

                        TrustManager trm = new X509TrustManager() {
                            public X509Certificate[] getAcceptedIssuers() {
                                return null;
                            }

                            public void checkClientTrusted(X509Certificate[] certs, String authType) {

                            }

                            public void checkServerTrusted(X509Certificate[] certs, String authType) {
                            }
                        };


                        SSLContext sc = SSLContext.getInstance("SSL");
                        sc.init(null, new TrustManager[]{trm}, null);

                        HttpsURLConnection con = (HttpsURLConnection) urlObj.openConnection();
                        con.setSSLSocketFactory(sc.getSocketFactory());
                        con.setHostnameVerifier(new NullHostNameVerifier());

                        InputStream inputStream = con.getInputStream();
                        OutputStream outputStream = new FileOutputStream(file);

                        try {
                            IOUtils.copy(inputStream, outputStream);
                        } finally {
                            IOUtils.closeQuietly(inputStream);
                            IOUtils.closeQuietly(outputStream);
                        }

                    } else {
                        URL urlObj = new URL(url);

                        HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();

                        InputStream inputStream = con.getInputStream();
                        OutputStream outputStream = new FileOutputStream(file);

                        try {
                            IOUtils.copy(inputStream, outputStream);
                        } finally {
                            IOUtils.closeQuietly(inputStream);
                            IOUtils.closeQuietly(outputStream);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callBack.onRequestFailed();
                }
            }
        }.start();
    }
}
