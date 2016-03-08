package contexts;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ConnectContext implements HttpHandler {

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		String response = "Welcome to Otak";
		
		try {
			httpExchange.sendResponseHeaders(200, response.length());

			OutputStream os = httpExchange.getResponseBody();
			os.write(response.getBytes());
			os.flush();
			os.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
