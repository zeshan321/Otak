package contexts;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.zeshanaslam.otak.Main;
import messages.Errors;
import utils.Config;
import utils.ServerUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class DownloadContext implements HttpHandler {

    private Config config = Main.config;

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        ServerUtils server = new ServerUtils();

        // Auth connection
        Map<String, String> params = server.queryToMap(httpExchange.getRequestURI().getQuery());

        if (!params.containsKey("pass") || !params.get("pass").equals(config.getString("pass"))) {
            server.writeResponse(httpExchange, new Errors().getError(Errors.ErrorTypes.AUTH));
            return;
        }

        // Params check
        if (!params.containsKey("file")) {
            server.writeResponse(httpExchange, new Errors().getError(Errors.ErrorTypes.MISSING));
            return;
        }

        // Send file
        new Thread() {
            @Override
            public void run() {
                File file = new File(config.getString("dir") + File.separator + params.get("file"));
                server.writeFile(httpExchange, file);
            }
        }.start();
    }
}
