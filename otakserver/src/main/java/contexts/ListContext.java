package contexts;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.zeshanaslam.otak.Main;
import messages.Errors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import utils.Config;
import utils.ServerUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class ListContext implements HttpHandler {

    private Config config = Main.config;

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        ServerUtils server = new ServerUtils();

        Map<String, String> params = server.queryToMap(httpExchange.getRequestURI().getRawQuery());

        // Auth connection
        if (!params.containsKey("pass") || !params.get("pass").equals(config.getString("pass"))) {
            server.writeResponse(httpExchange, new Errors().getError(Errors.ErrorTypes.AUTH));
            return;
        }

        // Start file list
        JSONArray jsonArray = new JSONArray();

        Collection<File> filesList = FileUtils.listFilesAndDirs(new File(config.getString("dir")), TrueFileFilter.TRUE, TrueFileFilter.TRUE);

        for (File fileIter : filesList) {
            String fileName = fileIter.getAbsolutePath().replace(config.getString("dir"), "").replaceAll("\\\\", "/");

            if (!fileName.equals("")) {
                if (fileIter.isDirectory()) {
                    jsonArray.put(jsonOutput(fileName, fileIter.lastModified(), true));
                } else {
                    jsonArray.put(jsonOutput(fileName, fileIter.lastModified(), false));
                }
            }
        }

        // size -1 to not include the main directory folder
        server.writeResponse(httpExchange, returnData(filesList.size() - 1, jsonArray));
    }

    private String returnData(int count, JSONArray array) {
        JSONObject jsonObject;
        String data = null;

        try {
            jsonObject = new JSONObject();
            jsonObject.put("success", true);
            jsonObject.put("count", count);
            jsonObject.put("info", array);

            data = jsonObject.toString(2);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return data;
    }

    private JSONObject jsonOutput(String file, long timestamp, boolean isDir) {
        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject();
            jsonObject.put("file", file);
            jsonObject.put("timestamp", timestamp);
            jsonObject.put("dir", isDir);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }
}
