package contexts;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.zeshanaslam.otak.Main;
import messages.Errors;
import org.json.JSONException;
import org.json.JSONObject;
import utils.Config;
import utils.ServerUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class ConnectContext implements HttpHandler {

    private Config config = Main.config;

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        new ServerUtils().writeResponse(httpExchange, jsonOutput());
    }

    private String jsonOutput() {
        JSONObject jsonObject;
        String data = null;

        try {
            jsonObject = new JSONObject();
            // Status needs to be worked on...
            jsonObject.put("status", "online");
            jsonObject.put("uuid", config.getString("UUID"));
            jsonObject.put("name", config.getString("name"));
            jsonObject.put("setup", config.getBoolean("setup"));

            data = jsonObject.toString(2);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return data;
    }
}
