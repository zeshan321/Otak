package requests;

import callbacks.DownloadCallback;
import org.apache.commons.io.IOUtils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.zip.GZIPInputStream;

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
                    // Create dirs
                    file.getParentFile().mkdirs();

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

                        HttpsURLConnection connection = (HttpsURLConnection) urlObj.openConnection();
                        connection.setSSLSocketFactory(sc.getSocketFactory());
                        connection.setHostnameVerifier(new NullHostNameVerifier());
                        connection.setRequestProperty("Accept-Encoding", "gzip");

                        GZIPInputStream inputStream = new GZIPInputStream(connection.getInputStream());
                        OutputStream outputStream = new FileOutputStream(file);

                        try {
                            ProgressListener progressListener = new ProgressListener();
                            DownloadOutputStream downloadCount = new DownloadOutputStream(outputStream, connection.getContentLength(), callBack);
                            downloadCount.setListener(progressListener);

                            copySteam(inputStream, downloadCount);
                        } finally {
                            IOUtils.closeQuietly(inputStream);
                            IOUtils.closeQuietly(outputStream);

                            callBack.onRequestComplete();
                        }

                    } else {
                        URL urlObj = new URL(url);

                        HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();

                        connection.setRequestProperty("Accept-Encoding", "gzip");

                        GZIPInputStream inputStream = new GZIPInputStream(connection.getInputStream());
                        OutputStream outputStream = new FileOutputStream(file);

                        try {
                            ProgressListener progressListener = new ProgressListener();
                            DownloadOutputStream downloadCount = new DownloadOutputStream(outputStream, connection.getContentLength(), callBack);
                            downloadCount.setListener(progressListener);

                            copySteam(inputStream, downloadCount);
                        } finally {
                            IOUtils.closeQuietly(inputStream);
                            IOUtils.closeQuietly(outputStream);

                            callBack.onRequestComplete();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callBack.onRequestFailed();
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

    private static class ProgressListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            DownloadOutputStream download = ((DownloadOutputStream) e.getSource());
            int percent = ((download.getCount() * 100) / download.fileLength);

            download.callback.onProgress(percent);
        }
    }
}
