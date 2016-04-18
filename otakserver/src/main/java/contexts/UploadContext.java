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

        final Map<String, String> params = server.queryToMap(httpExchange.getRequestURI().getRawQuery());

        System.out.println(params.get("pass"));
        System.out.println(params.get("file"));
        System.out.println(params.get("type"));

        // Auth connection
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

                server.writeResponse(httpExchange, returnData(true));
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

                server.writeResponse(httpExchange, returnData(true));
                break;

            case "delete":
                File fileDel = new File(config.getString("dir") + File.separator + params.get("file"));
                if (fileDel.isDirectory()) {
                    FileUtils.deleteDirectory(fileDel);
                } else {
                    fileDel.delete();
                }

                server.writeResponse(httpExchange, returnData(true));
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
}
