package requests;

import callback.HTTPCallback;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.URL;
import java.security.cert.X509Certificate;

public class HTTPUpload {

    private String url;
    private String USER_AGENT = "Mozilla/5.0";

    public HTTPUpload(String url) {
        this.url = url.replace(" ", "%20");
    }

    public void sendPost(File file,HTTPCallback callback) {
        try {
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
            con.setRequestProperty("User-Agent", USER_AGENT);

            con.setDoOutput(true);
            FileInputStream fileInputStream = new FileInputStream(file);

            try (DataOutputStream dataOutputStream = new DataOutputStream(con.getOutputStream())) {
                byte[] buffer = new byte[8192];
                int bytesRead;

                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    dataOutputStream.write(buffer, 0, bytesRead);
                }

                fileInputStream.close();
                dataOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }


            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String inputLine;
            StringBuilder stringBuilder = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                stringBuilder.append(inputLine);
            }
            in.close();

            callback.onSuccess(url, stringBuilder.toString());
        } catch (Exception e) {
            e.printStackTrace();
            callback.onError();
        }
    }
}