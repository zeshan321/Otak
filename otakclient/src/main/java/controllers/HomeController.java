package controllers;

import callbacks.DownloadCallback;
import callbacks.HTTPCallback;
import callbacks.NetworkChangeCallback;
import callbacks.TaskCallback;
import com.zeshanaslam.otak.Main;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import objects.FileObject;
import objects.QueueObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import requests.HTTPDownload;
import requests.HTTPGet;
import requests.HTTPUpload;
import utils.*;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.Paths;
import java.util.*;

public class HomeController implements Initializable {

    public Stage stage;
    public QueueManager queueManager;

    private DatagramSocket clientSocket;

    // Package local
    String currentDir = "";
    Config config;
    HashMap<String, List<FileObject>> filesMap = new HashMap<>();
    ContextMenu contextMenu = new ContextMenu();
    @FXML
    AnchorPane anchorPane;
    @FXML
    WebView webView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        config = Main.config;
        queueManager = new QueueManager(this);

        // Load site
        webView.getEngine().setJavaScriptEnabled(true);
        webView.getEngine().load("file:///" + Paths.get(".").toAbsolutePath().normalize().toString() + File.separator + "data" + File.separator + "home.html");

        // Make web view responsive
        new ResponsiveWeb(anchorPane, webView).makeResponsive();

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
                // Set servername
                runScript("setServerName(\"" + config.getString("name") + "\");");

                // Set JSObjects
                Document doc = webView.getEngine().getDocument();
                JSObject window = (JSObject) webView.getEngine().executeScript("window");

                // Home handler
                window.setMember("home", new HomeHandler(this, doc));

                // Load server files
                Data data = new Data();
                if (data.getContent() != null) {
                    loadFiles(data.getContent());
                }

                connect();
            }
        });

        // Drag and drop
        webView.setOnDragOver(event -> {
            event.acceptTransferModes(TransferMode.ANY);
            event.consume();
        });

        webView.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                success = true;
                for (File file : db.getFiles()) {
                    File newFile = new File(config.getString("dir") + File.separator + file.getName());

                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                if (file.isDirectory()) {
                                    newFile.mkdir();

                                    Collection<File> filesList = FileUtils.listFilesAndDirs(newFile, TrueFileFilter.TRUE, TrueFileFilter.TRUE);
                                    for (File files : filesList) {
                                        String loc = fixPath(files.getAbsolutePath());

                                        if (currentDir.equals("")) {
                                            queueManager.add(loc, new QueueObject(QueueObject.QueueType.UPLOAD, files));
                                        } else {
                                            queueManager.add(currentDir + "/" + loc, new QueueObject(QueueObject.QueueType.UPLOAD, files));
                                        }
                                    }

                                    FileUtils.copyDirectory(file, newFile);
                                } else {
                                    // Add to queue
                                    if (currentDir.equals("")) {
                                        queueManager.add(file.getName(), new QueueObject(QueueObject.QueueType.UPLOAD, newFile));
                                    } else {
                                        queueManager.add(currentDir + "/" + file.getName(), new QueueObject(QueueObject.QueueType.UPLOAD, newFile));
                                    }

                                    newFile.createNewFile();
                                    FileUtils.copyFile(file, newFile);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });

        // Server status checker
        new NetworkHandler(config.getString("IP")).run(new NetworkChangeCallback() {
            @Override
            public void onNetworkChange(NetworkType networkType) {
                switch (networkType) {
                    case ONLINE:
                        runScript("serverStatus('online');");

                        connect();
                        break;

                    case OFFLINE:
                        runScript("serverStatus('offline');");
                        break;

                    case UNKNOWN:

                        break;
                }
            }
        });
    }

    /**
     * Fix string to stay consistent to server path
     */
    public String fixPath(String path) {
        return path.replace(config.getString("dir"), "").replaceAll("\\\\", "/");
    }

    /**
     * Execute javascript on platform thread
     *
     * @param script javascript string
     */
    public void runScript(String script) {
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

    private void addItem(FileObject fileObject) {
        String path;
        if (!fileObject.file.contains("/")) {
            path = currentDir;
        } else {
            path = fileObject.file.substring(0, fileObject.file.lastIndexOf("/"));
        }

        List<FileObject> fileObjects = new ArrayList<>();
        if (filesMap.containsKey(path)) {
            fileObjects = filesMap.get(path);
            fileObjects.add(fileObject);

            filesMap.put(path, fileObjects);
        } else {
            fileObjects.add(fileObject);
            filesMap.put(path, fileObjects);
        }

        if (currentDir.equals(path)) {
            if (fileObject.isDir) {
                runScript("addItem('" + fileObject.file + "', '" + FilenameUtils.getName(fileObject.file) + "', 'folder');");
            } else {
                runScript("addItem('" + fileObject.file + "', '" + FilenameUtils.getName(fileObject.file) + "', '" + FilenameUtils.getExtension(fileObject.file).toLowerCase() + "');");
            }
        }
    }

    private void removeItem(FileObject fileObject) {
        String path;
        if (!fileObject.file.contains("/")) {
            path = currentDir;
        } else {
            path = fileObject.file.substring(0, fileObject.file.lastIndexOf("/"));
        }

        List<FileObject> files = filesMap.get(path);
        Iterator<FileObject> iterator = files.iterator();
        while (iterator.hasNext()) {
            FileObject fileObject1 = iterator.next();
            if (fileObject1.file.contains(fileObject.file)) {
                iterator.remove();
            }
        }

        filesMap.put(path, files);
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

    /**
     * Recursively get all files in list from directories
     *
     * @param files list of files
     * @param dirs  list of directories
     */
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
            for (String loc : files) {
                queueManager.add(loc, new QueueObject(QueueObject.QueueType.DOWNLOAD, new File(config.getString("dir") + File.separator + loc)));
            }
            return null;
        }

        return recursiveFileDownload(files, dirs);
    }

    public void downloadFile(File file, String loc, TaskCallback taskCallback) {
        runScript("addFileProgress('" + loc + "','download');");

        Parameters parameters = new Parameters();
        parameters.add("pass", config.getString("pass"));
        parameters.add("file", loc);
        parameters.add("sender", config.getString("UUID"));

        new HTTPDownload(config.getString("IP") + "/download", parameters.toString()).downloadFile(file, new DownloadCallback() {
            @Override
            public void onRequestComplete() {
                runScript("removeFileProgress('" + loc + "');");

                taskCallback.onComplete();
            }

            @Override
            public void onProgress(int progress) {

            }

            @Override
            public void onRequestFailed() {
                // Add to queue and try again later

                // Display error
                runScript("addFileProgress('" + loc + "','error');");

                taskCallback.onComplete();
            }
        });
    }

    public void torrentFile(File file, String loc, TaskCallback taskCallback) {
        runScript("addFileProgress('" + loc + "','torrent');");

        Parameters parameters = new Parameters();
        parameters.add("pass", config.getString("pass"));
        parameters.add("file", loc);
        parameters.add("sender", config.getString("UUID"));

        new HTTPGet(config.getString("IP") + "/torrent", parameters.toString()).sendGet(new HTTPCallback() {
            @Override
            public void onSuccess(String IP, String response) {
                runScript("removeFileProgress('" + loc + "');");

                taskCallback.onComplete();
            }

            @Override
            public void onError() {
                // Add to queue and try again later

                // Display error
                runScript("addFileProgress('" + loc + "','error');");

                taskCallback.onComplete();
            }
        });
    }


    public void uploadFile(File file, String loc, TaskCallback taskCallback) {
        runScript("addFileProgress('" + loc + "','upload');");

        Parameters parameters = new Parameters();
        parameters.add("pass", config.getString("pass"));
        parameters.add("file", loc);
        parameters.add("sender", config.getString("UUID"));

        // Set file type
        if (file.isDirectory()) {
            parameters.add("type", "dir");
        } else {
            parameters.add("type", "file");
        }

        new HTTPUpload(config.getString("IP") + "/upload", parameters.toString()).sendPost(file, new HTTPCallback() {
            @Override
            public void onSuccess(String IP, String response) {
                runScript("removeFileProgress('" + loc + "');");

                // Add to UI
                addItem(new FileObject(loc, file.isDirectory(), file.lastModified()));

                taskCallback.onComplete();
            }

            @Override
            public void onError() {
                // Display error
                runScript("addFileProgress('" + loc + "','error');");

                taskCallback.onComplete();
            }
        });
    }

    public void createFolder(File file, String loc, TaskCallback taskCallback) {
        runScript("addFileProgress('" + loc + "','upload');");

        Parameters parameters = new Parameters();
        parameters.add("pass", config.getString("pass"));
        parameters.add("file", loc);
        parameters.add("type", "dir");
        parameters.add("sender", config.getString("UUID"));

        new HTTPGet(config.getString("IP") + "/upload", parameters.toString()).sendGet(new HTTPCallback() {
            @Override
            public void onSuccess(String IP, String response) {
                runScript("removeFileProgress('" + loc + "');");

                // Add to UI
                addItem(new FileObject(loc, file.isDirectory(), file.lastModified()));

                taskCallback.onComplete();
            }

            @Override
            public void onError() {
                // Display error
                runScript("addFileProgress('" + loc + "','error');");

                taskCallback.onComplete();
            }
        });
    }

    public void deleteFile(File file, String loc, TaskCallback taskCallback) {
        runScript("addFileProgress('" + loc + "','delete');");

        Parameters parameters = new Parameters();
        parameters.add("pass", config.getString("pass"));
        parameters.add("file", loc);
        parameters.add("type", "delete");
        parameters.add("sender", config.getString("UUID"));

        new HTTPGet(config.getString("IP") + "/upload", parameters.toString()).sendGet(new HTTPCallback() {
            @Override
            public void onSuccess(String IP, String response) {
                runScript("removeFileProgress('" + loc + "');");

                // Remove from UI
                runScript("removeItem('" + loc + "');");
                removeItem(new FileObject(loc, file.isDirectory(), file.lastModified()));

                taskCallback.onComplete();
            }

            @Override
            public void onError() {
                // Display error
                runScript("addFileProgress('" + loc + "','error');");

                taskCallback.onComplete();
            }
        });
    }

    public void playerSelect(String loc, String type) {
        Parameters parameters = new Parameters();
        parameters.add("pass", config.getString("pass"));
        parameters.add("file", loc);
        parameters.add("type", type);

        String url = config.getString("IP") + "/stream" + parameters.toString();
        runScript("openPlayerSelect('" + url + "')");
    }

    private void connect() {
        // Load files
        Data data = new Data();

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
            }
        });

        // Start socket
        if (clientSocket != null) {
            clientSocket.disconnect();
            clientSocket.close();
        }

        // Initialize socket
        try {
            clientSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }

        new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        byte[] receiveData = new byte[1024];

                        DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
                        clientSocket.receive(packet);

                        String update = new String(packet.getData());
                        JSONObject json;

                        if (update.startsWith("Download: ")) {
                            json = new JSONObject(update.replace("Download: ", ""));
                            System.out.println("Download: " + json);

                            addItem(new FileObject(json.getString("file"), json.getBoolean("dir"), json.getLong("timestamp")));
                        }

                        if (update.startsWith("Delete: ")) {
                            json = new JSONObject(update.replace("Delete: ", ""));
                            System.out.println("Delete: " + json);

                            QueueObject queueObject = new QueueObject(QueueObject.QueueType.DELETE, new File(config.getString("dir") + File.separator + json.getString("file")));

                            // Remove from UI
                            runScript("removeItem('" + json.getString("file") + "');");
                            removeItem(new FileObject(json.getString("file"), queueObject.file.isDirectory(), queueObject.file.lastModified()));

                            // Remove local file
                            if (queueObject.file.isDirectory()) {
                                try {
                                    FileUtils.deleteDirectory(queueObject.file);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                queueObject.file.delete();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

        // Send server client UUID
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("UUID", config.getString("UUID"));
        sendMessage("Ping: " + jsonObject);
    }

    /**
     * Send message to server socket
     */
    private void sendMessage(String message) {
        try {
            URL url = new URL(config.getString("IP"));
            InetAddress address = InetAddress.getByName(url.getHost());

            byte buff[] = message.getBytes();
            DatagramPacket packetSend = new DatagramPacket(buff, buff.length, address, url.getPort());

            clientSocket.send(packetSend);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
