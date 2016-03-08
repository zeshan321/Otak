package requests;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

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
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

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
