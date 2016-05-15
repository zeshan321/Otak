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
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import objects.FileObject;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLInputElement;
import requests.HTTPDownload;
import requests.HTTPGet;
import utils.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    public Stage stage;

    private String currentDir = "";
    private HashMap<String, List<FileObject>> filesMap = new HashMap<>();
    private Config config;
    private List<String> filesDownloading = new ArrayList<>();
    private ContextMenu contextMenu = new ContextMenu();

    // Threads
    private int connectionLimit = 5;
    private int threadsRunning = 0;

    @FXML
    private AnchorPane anchorPane;
    @FXML
    private WebView webView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Initialized");
        config = Main.config;

        // Load site
        webView.getEngine().setJavaScriptEnabled(true);
        webView.getEngine().load("file:///" + Paths.get(".").toAbsolutePath().normalize().toString() + File.separator + "data" + File.separator + "home.html");

        // Make web view responsive
        new ResponsiveWeb(anchorPane, webView).makeResponsive();

        WebDispatcher webDispatcher = new WebDispatcher(webView.getEventDispatcher());
        // Wait for UI to finish loading
        webView.getEngine().getLoadWorker().stateProperty().addListener((observableValue, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                // Dispatch web events
                webView.setEventDispatcher(webDispatcher);

                Document doc = webView.getEngine().getDocument();

                // Listen for file clicks
                JSObject window = (JSObject) webView.getEngine().executeScript("window");
                window.setMember("file", this);

                // Settings
                final Element settingsButton = doc.getElementById("menu-settings");
                ((EventTarget) settingsButton).addEventListener("click", evt -> {

                    // Server ip
                    HTMLInputElement input = (HTMLInputElement) doc.getElementById("serverIP");
                    input.setAttribute("value", config.getString("IP"));

                    // Password
                    input = (HTMLInputElement) doc.getElementById("password");
                    input.setAttribute("value", config.getString("pass"));

                    // Dir
                    input = (HTMLInputElement) doc.getElementById("dir");
                    input.setAttribute("value", config.getString("dir"));

                    // Auto sync
                    input = (HTMLInputElement) doc.getElementById("auto-sync");
                    input.setChecked(config.getBoolean("sync"));
                }, false);

                final HTMLInputElement dirInput = (HTMLInputElement) doc.getElementById("dir");
                ((EventTarget) dirInput).addEventListener("click", evt -> {
                    DirectoryChooser dirChooser = new DirectoryChooser();
                    dirChooser.setTitle("Select Otak Installation Directory");

                    File file = dirChooser.showDialog(stage);
                    if (file != null) {
                        dirInput.setAttribute("value", file.getPath());
                    }
                }, false);

                final Element saveButton = doc.getElementById("btn-save");
                ((EventTarget) saveButton).addEventListener("click", evt -> {

                    // Server ip
                    HTMLInputElement input = (HTMLInputElement) doc.getElementById("serverIP");
                    config.set("IP", input.getValue());

                    // Password
                    input = (HTMLInputElement) doc.getElementById("password");
                    config.set("pass", input.getValue());

                    // Dir
                    input = (HTMLInputElement) doc.getElementById("dir");
                    config.set("dir", input.getValue());

                    // Auto sync
                    input = (HTMLInputElement) doc.getElementById("auto-sync");
                    config.set("sync", input.getChecked());

                    config.save();
                }, false);

                // Load server files
                Data data = new Data();

                // Load local files
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

                // Reset directory on home click
                final Element homeButton = doc.getElementById("home1");
                ((EventTarget) homeButton).addEventListener("click", evt -> {
                    if (!currentDir.equals("")) {
                        currentDir = "";
                        parseMap();
                    }
                }, false);

                final Element home1Button = doc.getElementById("home2");
                ((EventTarget) home1Button).addEventListener("click", evt -> {
                    if (!currentDir.equals("")) {
                        currentDir = "";
                        parseMap();
                    }
                }, false);

                // Back directory
                final Element backButton = doc.getElementById("btn-back");
                ((EventTarget) backButton).addEventListener("click", evt -> {
                    if (!currentDir.equals("")) {
                        currentDir = currentDir.substring(0, currentDir.lastIndexOf("/"));
                        parseMap();
                    }
                }, false);

                // Hide context menu
                webView.setOnMousePressed(e -> {
                    if (e.getButton() == MouseButton.PRIMARY) {
                        if (contextMenu != null) {
                            contextMenu.hide();
                        }
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
    private void parseMap() {
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

    /**
     * Runs on file clicks from UI
     *
     * @param click Mouse click type
     * @param loc   File location
     * @param name  File name
     * @param type  File extension
     */
    public void onClick(String click, String loc, String name, String type) {
        if (click.equals("left")) {
            contextMenu.hide();

            if (type.equals("folder")) {
                currentDir = loc;
                parseMap();
            } else {
                File file = new File(config.getString("dir") + File.separator + loc);
                if (file.exists()) {
                    try {
                        Desktop.getDesktop().open(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }

                // Download file
                if (!filesDownloading.contains(loc)) {
                    downloadFile(file, loc, new TaskCallback() {
                        @Override
                        public void onComplete() {

                        }
                    });
                }
            }
        } else {
            contextMenu.hide();
            contextMenu = new ContextMenu();

            MenuItem menuItem = new MenuItem("Delete");

            if (type.equals("folder")) {
                MenuItem download = new MenuItem("Download All");

                download.setOnAction(event -> {
                    List<String> files = new ArrayList<>();
                    List<String> dirs = new ArrayList<>();
                    dirs.add(loc);

                    recursiveFileDownload(files, dirs);
                });

                contextMenu.getItems().add(download);
            }

            contextMenu.getItems().add(menuItem);
            contextMenu.show(webView, MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y);
        }
    }

    private List<FileObject> recursiveFileDownload(List<String> files, List<String> dirs) {
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
                    for (String loc: files) {
                        runScript("addFileProgress('" + loc + "','queue');");
                    }

                    int currentFile = -1;

                    while (true) {
                        System.out.println(threadsRunning);
                        if (threadsRunning <= connectionLimit) {
                            if (currentFile == files.size() -1) {
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

    private void downloadFile(File file, String loc, TaskCallback taskCallback) {
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
