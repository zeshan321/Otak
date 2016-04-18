package utils;

import callback.DownloadCallback;
import callback.HTTPCallback;
import objects.FileObject;
import org.json.JSONArray;
import org.json.JSONObject;
import requests.HTTPDownload;
import requests.HTTPGet;
import requests.HTTPUpload;
import sync.Compare;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileSync implements Runnable {

    private Config config;
    private String json;

    public FileSync(String json) {
        this.json = json;
        config = new Config();
    }

    @Override
    public void run() {
        List<FileObject> fileObjects = filesNeeded();

        String IP = config.getString("IP");
        String pass = config.getString("pass");
        for (FileObject fileObject : fileObjects) {
            File file = new File(config.getString("dir") + File.separator + fileObject.file);

            if (fileObject.isDir) {
                file.mkdirs();
            } else {
                new HTTPDownload(IP + "/download?pass=" + pass + "&file=" + fileObject.file).downloadFile(file, new DownloadCallback() {
                    @Override
                    public void onRequestComplete() {
                        // Set last modified to correct timestamp
                        file.setLastModified(fileObject.timestamp);
                    }

                    @Override
                    public void onProgress(int progress) {

                    }

                    @Override
                    public void onRequestFailed() {

                    }
                });
            }
        }

        // Watch for directory changes
        dirWatcher();
    }

    private List<FileObject> filesNeeded() {
        JSONObject jsonObject = new JSONObject(json);
        JSONArray jsonArray = jsonObject.getJSONArray("info");

        List<FileObject> files = new ArrayList<>();

        for (int n = 0; n < jsonArray.length(); n++) {
            JSONObject jsonFile = jsonArray.getJSONObject(n);

            File file = new File(config.getString("dir") + File.separator + jsonFile.getString("file"));

            if (file.exists()) {
                Timestamp serverStamp = new Timestamp(jsonFile.getLong("timestamp"));
                Timestamp clientStamp = new Timestamp(file.lastModified());

                // Skip if file timestamps are the same
                if (clientStamp.after(serverStamp) && clientStamp.before(serverStamp)) {
                    continue;
                }

                if (serverStamp.after(clientStamp)) {
                    // Skip file update if its a dir
                    if (!jsonFile.getBoolean("dir")) {
                        files.add(new FileObject(jsonFile.getString("file"), jsonFile.getBoolean("dir"), jsonFile.getLong("timestamp")));
                    }
                }
            } else {
                files.add(new FileObject(jsonFile.getString("file"), jsonFile.getBoolean("dir"), jsonFile.getLong("timestamp")));
            }
        }

        return files;
    }

    private void dirWatcher() {
        new Thread() {
            @Override
            public void run() {
                Path path = Paths.get(config.getString("dir"));

                try {
                    WatchService watcher = path.getFileSystem().newWatchService();

                    // Watch every sub-dir in path
                    Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                            dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                            return FileVisitResult.CONTINUE;
                        }
                    });

                    WatchKey watchKey;

                    while (true) {
                        watchKey = watcher.take();

                        List<WatchEvent<?>> events = watchKey.pollEvents();
                        for (WatchEvent event : events) {
                            Path dir = (Path) watchKey.watchable();

                            if (event.kind() == OVERFLOW) {
                                continue;
                            }

                             else if (event.kind() == ENTRY_CREATE) {
                                Path fullPath = dir.resolve((Path) event.context());
                                File file = fullPath.toFile();

                                System.out.println("Create: " + fullPath);
                                if (file.isDirectory()) {
                                    // Watch new directory
                                    path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

                                    updateFile(fixPath(fullPath.toString()), "dir");
                                } else {
                                    uploadFile(file, fixPath(fullPath.toString()), "file");
                                }
                            }
                            else if (event.kind() == ENTRY_DELETE) {
                                Path fullPath = dir.resolve((Path) event.context());

                                updateFile(fixPath(fullPath.toString()), "delete");
                            }
                            else if (event.kind() == ENTRY_MODIFY) {
                                Path fullPath = dir.resolve((Path) event.context());
                                File file = fullPath.toFile();

                                if (!file.isDirectory()) {
                                    uploadFile(file, fixPath(fullPath.toString()), "file");
                                }
                            }
                        }

                        if(!watchKey.reset()) {
                            break; //loop
                        }
                    }

                } catch (Exception e) {
                    System.out.println("Error: " + e.toString());
                }
            }
        }.start();
    }

    private String fixPath(String path) {
        return path.replace(config.getString("dir"), "").replaceAll("\\\\", "/");
    }

    private void updateFile(String file, String type) {
        new HTTPGet(config.getString("IP") + "/upload?pass=" + config.getString("pass") + "&file=" + file + "&type=" + type).sendGet(new HTTPCallback() {
            @Override
            public void onSuccess(String IP, String response) {

            }

            @Override
            public void onError() {
                System.out.println("Error!");
            }
        });
    }

    private void uploadFile(File fileUP, String file, String type) {
        new HTTPUpload(config.getString("IP") + "/upload?pass=" + config.getString("pass") + "&file=" + file + "&type=" + type).sendPost(fileUP, new HTTPCallback() {
            @Override
            public void onSuccess(String IP, String response) {
                System.out.println(response);
            }

            @Override
            public void onError() {
                System.out.println("Error! 1");
            }
        });
    }
}
