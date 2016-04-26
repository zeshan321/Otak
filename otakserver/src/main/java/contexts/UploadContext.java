package contexts;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.zeshanaslam.otak.Main;
import messages.Errors;
import org.apache.commons.io.FileUtils;
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

        final Map<String, String> params = server.queryToMap(httpExchange.getRequestURI().getRawQuery());

        // Auth connection
        if (!params.containsKey("pass") || !params.get("pass").equals(config.getString("pass"))) {
            server.writeResponse(httpExchange, new Errors().getError(Errors.ErrorTypes.AUTH));
            return;
        }

        // Params check
        if (!params.containsKey("file") || !params.containsKey("type") || !params.containsKey("sender")) {
            server.writeResponse(httpExchange, new Errors().getError(Errors.ErrorTypes.MISSING));
            return;
        }

        switch (params.get("type")) {
            case "dir":
                File file = new File(config.getString("dir") + File.separator + params.get("file"));
                file.mkdirs();

                server.writeResponse(httpExchange, returnData(true));
                Main.sendMessage("Download: " + fileJSON(params.get("file"), file.lastModified(), file.isDirectory()), params.get("sender"));
                break;

            case "file":
                // Needs new thread
                file = new File(config.getString("dir") + File.separator + params.get("file"));

                if (file.exists()) {
                    file.delete();
                }

                InputStream inputStream = httpExchange.getRequestBody();
                OutputStream outputStream = new FileOutputStream(file);

                server.copySteam(inputStream, outputStream);

                server.writeResponse(httpExchange, returnData(true));
                Main.sendMessage("Download: " + fileJSON(params.get("file"), file.lastModified(), file.isDirectory()), params.get("sender"));
                break;

            case "delete":
                File fileDel = new File(config.getString("dir") + File.separator + params.get("file"));

                long timestamp = fileDel.lastModified();
                boolean isDir = fileDel.isDirectory();

                if (fileDel.isDirectory()) {
                    FileUtils.deleteDirectory(fileDel);
                } else {
                    fileDel.delete();
                }

                server.writeResponse(httpExchange, returnData(true));
                Main.sendMessage("Delete: " + fileJSON(params.get("file"), timestamp, isDir), params.get("sender"));
                break;
            default:
                server.writeResponse(httpExchange, new Errors().getError(Errors.ErrorTypes.MISSING));
        }
    }

    private String returnData(boolean status) {
        JSONObject jsonObject;
        String data = null;

        try {
            jsonObject = new JSONObject();
            jsonObject.put("success", status);
            data = jsonObject.toString(2);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return data;
    }

    private String fileJSON(String file, long timestamp, boolean isDir) {
        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject();
            jsonObject.put("file", file);
            jsonObject.put("timestamp", timestamp);
            jsonObject.put("dir", isDir);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject.toString();
    }
}
