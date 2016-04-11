package utils;

import com.sun.net.httpserver.HttpExchange;
import com.zeshanaslam.otak.Main;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class ServerUtils {

    private HttpExchange httpExchange;

    public ServerUtils(HttpExchange httpExchange) {
        this.httpExchange = httpExchange;
    }

    public void writeResponse(String text) {
        try {
            httpExchange.sendResponseHeaders(200, text.length());

            OutputStream os = httpExchange.getResponseBody();
            os.write(text.getBytes());
            os.flush();
            os.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String pair[] = param.split("=");
            if (pair.length > 1) {
                result.put(pair[0], pair[1]);
            } else {
                result.put(pair[0], "");
            }
        }
        return result;
    }

    public boolean authClient(String password) {
        Config config = Main.config;

        return config.getString("pass").equals(password);
    }
}
