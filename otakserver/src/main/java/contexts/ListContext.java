package contexts;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.zeshanaslam.otak.Main;
import messages.Errors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.json.JSONException;
import org.json.JSONObject;
import utils.Config;
import utils.ServerUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ListContext implements HttpHandler {

    private Config config = Main.config;

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        ServerUtils server = new ServerUtils();

        // Auth connection
        Map<String, String> params = server.queryToMap(httpExchange.getRequestURI().getQuery());

        if (!params.containsKey("pass") || !params.get("pass").equals(config.getString("pass"))) {
            server.writeResponse(httpExchange, new Errors().getError(Errors.ErrorTypes.Auth));
            return;
        }

        // Start file list
        Collection<File> filesList = FileUtils.listFilesAndDirs(new File(config.getString("dir")), TrueFileFilter.TRUE, TrueFileFilter.TRUE);
        List<String> dirs = new ArrayList<>();
        List<String> files = new ArrayList<>();

        for (File fileIter : filesList) {
            if (fileIter.isDirectory()) {
                dirs.add(fileIter.getAbsolutePath().replace(config.getString("dir"), ""));
            } else {
                files.add(fileIter.getAbsolutePath().replace(config.getString("dir"), ""));
            }
        }

        // Remove empty strings
        dirs.remove("");

        // size -1 to not include the main directory folder
        server.writeResponse(httpExchange, jsonOutput(filesList.size() - 1, dirs, files));
    }

    private String jsonOutput(int count, List dirs, List files) {
        JSONObject jsonObject;
        String data = null;

        try {
            jsonObject = new JSONObject();
            jsonObject.put("success", true);
            jsonObject.put("count", count);
            jsonObject.put("dirs", dirs);
            jsonObject.put("files", files);

            data = jsonObject.toString(2);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return data;
    }
}
