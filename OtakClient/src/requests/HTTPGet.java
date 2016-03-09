package requests;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HTTPGet {

	private String url;
	private String USER_AGENT = "Mozilla/5.0";

	public HTTPGet(String url) {
		this.url = url;
	}

	public String sendGet() {
		String response = null;
		try {
			URL obj = new URL(url);
			
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
            
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

			con.setHostnameVerifier(new NullHostNameVerifier());
			con.setSSLSocketFactory(sslContext.getSocketFactory());
			
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", USER_AGENT);

			BufferedReader in = new BufferedReader(
					new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer responseBuffer = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				responseBuffer.append(inputLine);
			}
			in.close();
			
			return responseBuffer.toString();
		} catch (Exception e) {
			return response;
		}		
	}
}
