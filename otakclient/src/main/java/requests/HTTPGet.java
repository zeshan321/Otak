package requests;

import callbacks.HTTPCallback;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.zip.GZIPInputStream;

public class HTTPGet {

    private String url;
    private String USER_AGENT = "Mozilla/5.0";

    public HTTPGet(String url, String params) {
        this.url = url + params;
    }

    public HTTPGet(String url) {
        this.url = url;
    }

    public void sendGet(HTTPCallback callback) {
        new Thread() {
            @Override
            public void run() {
                try {
                    // Check if to use https or not
                    if (url.startsWith("HTTPS://") || url.startsWith("https://")) {
                        URL urlObj = new URL(url);

                        TrustManager trustManager = new X509TrustManager() {
                            public X509Certificate[] getAcceptedIssuers() {
                                return null;
                            }

                            public void checkClientTrusted(X509Certificate[] certs, String authType) {

                            }

                            public void checkServerTrusted(X509Certificate[] certs, String authType) {
                            }
                        };


                        SSLContext sslContext = SSLContext.getInstance("SSL");
                        sslContext.init(null, new TrustManager[]{trustManager}, null);

                        HttpsURLConnection connection = (HttpsURLConnection) urlObj.openConnection();

                        connection.setHostnameVerifier(new NullHostNameVerifier());
                        connection.setSSLSocketFactory(sslContext.getSocketFactory());

                        connection.setRequestProperty("Accept-Encoding", "gzip");
                        connection.setRequestMethod("GET");
                        connection.setRequestProperty("User-Agent", USER_AGENT);

                        GZIPInputStream gzipInputStream = new GZIPInputStream(connection.getInputStream());
                        BufferedReader in = new BufferedReader(new InputStreamReader(gzipInputStream));

                        String inputLine;
                        StringBuilder stringBuilder = new StringBuilder();

                        while ((inputLine = in.readLine()) != null) {
                            stringBuilder.append(inputLine);
                        }
                        in.close();

                        callback.onSuccess(url, stringBuilder.toString());
                    } else {
                        URL urlObj = new URL(url);
                        HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();

                        connection.setRequestProperty("Accept-Encoding", "gzip");
                        connection.setRequestMethod("GET");
                        connection.setRequestProperty("User-Agent", USER_AGENT);

                        GZIPInputStream gzipInputStream = new GZIPInputStream(connection.getInputStream());
                        BufferedReader in = new BufferedReader(new InputStreamReader(gzipInputStream));

                        String inputLine;
                        StringBuilder stringBuilder = new StringBuilder();

                        while ((inputLine = in.readLine()) != null) {
                            stringBuilder.append(inputLine);
                        }
                        in.close();

                        callback.onSuccess(url, stringBuilder.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onError();
                }
            }
        }.start();
    }
}
