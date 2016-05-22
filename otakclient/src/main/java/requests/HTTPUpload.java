package requests;

import callback.HTTPCallback;
import org.apache.commons.io.IOUtils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class HTTPUpload {

    private String url;
    private String USER_AGENT = "Mozilla/5.0";

    public HTTPUpload(String url, String params) {
        this.url = url + params;
    }

    public void sendPost(File file, HTTPCallback callback) {
        new Thread() {
            @Override
            public void run() {
                try {
                    // Wait for file to be ready. Change this!
                    boolean success = file.renameTo(file);
                    while (!success) {

                    }

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

                        HttpsURLConnection con = (HttpsURLConnection) urlObj.openConnection();

                        con.setHostnameVerifier(new NullHostNameVerifier());
                        con.setSSLSocketFactory(sslContext.getSocketFactory());

                        con.setRequestMethod("POST");
                        con.setRequestProperty("Content-Encoding", "gzip");
                        con.setRequestProperty("User-Agent", USER_AGENT);

                        con.setDoOutput(true);
                        FileInputStream fileInputStream = new FileInputStream(file);
                        GZIPOutputStream outputStream = new GZIPOutputStream(con.getOutputStream());

                        try {
                            copySteam(fileInputStream, outputStream);
                        } finally {
                            IOUtils.closeQuietly(fileInputStream);
                            IOUtils.closeQuietly(outputStream);
                        }


                        GZIPInputStream gzipInputStream = new GZIPInputStream(con.getInputStream());
                        BufferedReader in = new BufferedReader(new InputStreamReader(gzipInputStream));

                        String inputLine;
                        StringBuilder stringBuilder = new StringBuilder();

                        while ((inputLine = in.readLine()) != null) {
                            stringBuilder.append(inputLine);
                        }
                        in.close();

                        IOUtils.closeQuietly(con.getInputStream());
                        callback.onSuccess(url, stringBuilder.toString());
                    } else {
                        URL urlObj = new URL(url);
                        HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();

                        con.setRequestMethod("POST");
                        con.setRequestProperty("User-Agent", USER_AGENT);

                        con.setDoOutput(true);
                        FileInputStream fileInputStream = new FileInputStream(file);
                        GZIPOutputStream outputStream = new GZIPOutputStream(con.getOutputStream());

                        try {
                            copySteam(fileInputStream, outputStream);
                        } finally {
                            IOUtils.closeQuietly(fileInputStream);
                            IOUtils.closeQuietly(outputStream);
                        }

                        GZIPInputStream gzipInputStream = new GZIPInputStream(con.getInputStream());
                        BufferedReader in = new BufferedReader(new InputStreamReader(gzipInputStream));

                        String inputLine;
                        StringBuilder stringBuilder = new StringBuilder();

                        while ((inputLine = in.readLine()) != null) {
                            stringBuilder.append(inputLine);
                        }
                        in.close();

                        IOUtils.closeQuietly(con.getInputStream());
                        callback.onSuccess(url, stringBuilder.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onError();
                }
            }
        }.start();
    }

    public void copySteam(InputStream inputStream, OutputStream outputStream) throws IOException {
        try {
            int len;
            byte[] buffer = new byte[8192];
            while ((len = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }

            inputStream.close();

            outputStream.flush();
            outputStream.close();
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
    }
}
