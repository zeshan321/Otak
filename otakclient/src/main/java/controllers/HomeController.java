package controllers;

import callback.DownloadCallback;
import callback.HTTPCallback;
import callback.TaskCallback;
import com.zeshanaslam.otak.Main;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import objects.FileObject;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import requests.HTTPDownload;
import requests.HTTPGet;
import utils.*;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    public Stage stage;

    // Package local
    String currentDir = "";
    Config config;
    HashMap<String, List<FileObject>> filesMap = new HashMap<>();
    List<String> filesDownloading = new ArrayList<>();
    ContextMenu contextMenu = new ContextMenu();
    @FXML
    AnchorPane anchorPane;
    @FXML
    WebView webView;

    // Threads
    private int connectionLimit = 5;
    private int threadsRunning = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        config = Main.config;

        // Load site
        webView.getEngine().setJavaScriptEnabled(true);
        webView.getEngine().load("file:///" + Paths.get(".").toAbsolutePath().normalize().toString() + File.separator + "data" + File.separator + "home.html");

        // Make web view responsive
        new ResponsiveWeb(anchorPane, webView).makeResponsive();

        WebDispatcher webDispatcher = new WebDispatcher(webView.getEventDispatcher());

        // Hide context menu
        webView.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                if (contextMenu != null) {
                    contextMenu.hide();
                }
            }
        });
        // Wait for UI to finish loading
        webView.getEngine().getLoadWorker().stateProperty().addListener((observableValue, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                // Dispatch web events
                webView.setEventDispatcher(webDispatcher);

                Document doc = webView.getEngine().getDocument();
                JSObject window = (JSObject) webView.getEngine().executeScript("window");


                // Home handler
                window.setMember("home", new HomeHandler(this, doc));

                // Load server files
                Data data = new Data();
                if (data.getContent() != null) {
                    loadFiles(data.getContent());
                }

                Parameters parameters = new Parameters();
                parameters.add("pass", config.getString("pass"));

                new HTTPGet(config.getString("IP") + "/list", parameters.toString()).sendGet(new HTTPCallback() {
                    @Override
                    public void onSuccess(String IP, String response) {
                        // Server status offline
                        runScript("serverStatus('online');");

                        JSONObject jsonObject = new JSONObject(response);

                        if (jsonObject.getBoolean("success")) {
                            data.set(response);
                            data.save();

                            loadFiles(data.getContent());
                        } else {
                            if (data.getContent() != null) {
                                loadFiles(data.getContent());
                            }
                        }
                    }

                    @Override
                    public void onError() {
                        // Server status offline
                        runScript("serverStatus('offline');");
                    }
                });
            }
        });
    }

    /**
     * Execute javascript on platform thread
     *
     * @param script javascript string
     */
    private void runScript(String script) {
        Platform.runLater(() -> webView.getEngine().executeScript(script));
    }

    /**
     * Group files by directory in HashMap
     *
     * @param content json content of all files
     */
    private void loadFiles(String content) {
        filesMap.clear();

        JSONObject jsonObject = new JSONObject(content);
        JSONArray serverArray = jsonObject.getJSONArray("info");

        for (int n = 0; n < serverArray.length(); n++) {
            JSONObject clientFile = serverArray.getJSONObject(n);
            FileObject fileObject = new FileObject(clientFile.getString("file"), clientFile.getBoolean("dir"), clientFile.getLong("timestamp"));

            String path = fileObject.file.substring(0, fileObject.file.lastIndexOf("/"));
            List<FileObject> fileObjects = new ArrayList<>();
            if (filesMap.containsKey(path)) {
                fileObjects = filesMap.get(path);
                fileObjects.add(fileObject);

                filesMap.put(path, fileObjects);
            } else {
                fileObjects.add(fileObject);
                filesMap.put(path, fileObjects);
            }
        }

        parseMap();
    }

    /**
     * Parse filesMap and display on UI
     */
    void parseMap() {
        runScript("clearItems();");

        filesMap.keySet().stream().filter(keys -> keys.equals(currentDir)).forEach(keys -> {
            for (FileObject fileObject : filesMap.get(keys)) {
                if (fileObject.isDir) {
                    runScript("addItem('" + fileObject.file + "', '" + FilenameUtils.getName(fileObject.file) + "', 'folder');");
                } else {
                    runScript("addItem('" + fileObject.file + "', '" + FilenameUtils.getName(fileObject.file) + "', '" + FilenameUtils.getExtension(fileObject.file).toLowerCase() + "');");
                }
            }
        });
    }

    List<FileObject> recursiveFileDownload(List<String> files, List<String> dirs) {
        String dir;
        if (dirs.size() > 0) {
            dir = dirs.get(0);
            dirs.remove(0);

            String finalDir = dir;
            filesMap.keySet().stream().filter(keys -> keys.equals(finalDir)).forEach(keys -> {
                for (FileObject fileObject : filesMap.get(keys)) {
                    if (fileObject.isDir) {
                        dirs.add(fileObject.file);
                    } else {
                        files.add(fileObject.file);
                    }
                }
            });
        } else {
            // Download and limit threads
            new Thread() {
                @Override
                public void run() {
                    // Add files to queue
                    for (String loc : files) {
                        runScript("addFileProgress('" + loc + "','queue');");
                    }

                    int currentFile = -1;

                    while (true) {
                        if (threadsRunning <= connectionLimit) {
                            if (currentFile == files.size() - 1) {
                                break;
                            }

                            threadsRunning++;
                            currentFile++;

                            downloadFile(new File(config.getString("dir") + File.separator + files.get(currentFile)), files.get(currentFile), new TaskCallback() {
                                @Override
                                public void onComplete() {
                                    threadsRunning--;
                                }
                            });
                        }
                    }
                }
            }.start();
            return null;
        }

        return recursiveFileDownload(files, dirs);
    }

    void downloadFile(File file, String loc, TaskCallback taskCallback) {
        runScript("addFileProgress('" + loc + "','download');");

        // Add to list
        filesDownloading.add(loc);

        Parameters parameters = new Parameters();
        parameters.add("pass", config.getString("pass"));
        parameters.add("file", loc);
        parameters.add("sender", config.getString("UUID"));

        new HTTPDownload(config.getString("IP") + "/download", parameters.toString()).downloadFile(file, new DownloadCallback() {
            @Override
            public void onRequestComplete() {
                runScript("removeFileProgress('" + loc + "');");

                // Remove from list
                filesDownloading.remove(loc);

                taskCallback.onComplete();
            }

            @Override
            public void onProgress(int progress) {

            }

            @Override
            public void onRequestFailed() {
                // Add to queue and try again later

                // Remove from list
                filesDownloading.remove(loc);

                // Display error
                runScript("addFileProgress('" + loc + "','error');");

                taskCallback.onComplete();
            }
        });
    }
}
