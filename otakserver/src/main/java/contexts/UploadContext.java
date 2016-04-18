package contexts;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.zeshanaslam.otak.Main;
import messages.Errors;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import utils.Config;
import utils.ServerUtils;

import java.io.*;
import java.util.Map;

public class UploadContext implements HttpHandler {

    private Config config = Main.config;

    @Override
    public void handle(final HttpExchange httpExchange) throws IOException {
        final ServerUtils server = new ServerUtils();

        // Auth connection
        final Map<String, String> params = server.queryToMap(httpExchange.getRequestURI().getQuery());

        if (!params.containsKey("pass") || !params.get("pass").equals(config.getString("pass"))) {
            server.writeResponse(httpExchange, new Errors().getError(Errors.ErrorTypes.AUTH));
            return;
        }

        // Params check
        if (!params.containsKey("file") || !params.containsKey("type")) {
            server.writeResponse(httpExchange, new Errors().getError(Errors.ErrorTypes.MISSING));
            return;
        }

        switch (params.get("type")) {
            case "dir":
                new File(config.getString("dir") + File.separator + params.get("file")).mkdirs();
                break;

            case "file":
                // Needs new thread
                File file = new File(config.getString("dir") + File.separator + params.get("file"));

                if (file.exists()) {
                    file.delete();
                }

                InputStream inputStream = httpExchange.getRequestBody();
                OutputStream outputStream = new FileOutputStream(file);

                byte data[] = new byte[8192];

                int count;
                while ((count = inputStream.read(data)) != -1) {
                    outputStream.write(data, 0, count);
                }

                outputStream.flush();
                outputStream.close();
                inputStream.close();
                break;

            case "delete":
                File fileDel = new File(config.getString("dir") + File.separator + params.get("file"));
                if (fileDel.isDirectory()) {
                    FileUtils.deleteDirectory(fileDel);
                } else {
                    fileDel.delete();
                }
                break;
            default:
                server.writeResponse(httpExchange, new Errors().getError(Errors.ErrorTypes.MISSING));
        }

        server.writeResponse(httpExchange, returnData());
    }

    private String returnData() {
        JSONObject jsonObject;
        String data = null;

        try {
            jsonObject = new JSONObject();
            jsonObject.put("success", true);
            data = jsonObject.toString(2);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return data;
    }
}
