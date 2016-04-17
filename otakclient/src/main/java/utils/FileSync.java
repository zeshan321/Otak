package utils;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import callback.DownloadCallback;
import objects.FileObject;
import org.json.JSONArray;
import org.json.JSONObject;
import requests.HTTPDownload;

import java.io.File;
import java.nio.file.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class FileSync implements Runnable {

    private Config config;
    private String json;

    public FileSync(String json) {
        this.json = json;
        config = new Config();

        // Watch for directory changes
        dirWatcher();
    }

    @Override
    public void run() {
        List<FileObject> fileObjects = filesNeeded();

        String IP = config.getString("IP");
        String pass = config.getString("pass");
        for (FileObject fileObject: fileObjects) {
            File file = new File(config.getString("dir") + File.separator + fileObject.file);
            // Set last modified to correct timestamp
            file.setLastModified(fileObject.timestamp);

            if (fileObject.isDir) {
                file.mkdirs();
            } else {
                new HTTPDownload(IP + "/download?pass=" + pass + "&file=" + fileObject.file).downloadFile(file, new DownloadCallback() {
                    @Override
                    public void onRequestComplete() {

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
                if(clientStamp.after(serverStamp) && clientStamp.before(serverStamp)) {
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
        Path path = Paths.get(config.getString("dir"));

        try {
            WatchService watcher = path.getFileSystem().newWatchService();
            path.register(watcher, ENTRY_CREATE,
                    ENTRY_DELETE, ENTRY_MODIFY);

            WatchKey watchKey = watcher.take();

            List<WatchEvent<?>> events = watchKey.pollEvents();
            for (WatchEvent event : events) {
                if (event.kind() == ENTRY_CREATE) {
                    System.out.println("Created: " + event.context().toString());
                }
                if (event.kind() == ENTRY_DELETE) {
                    System.out.println("Delete: " + event.context().toString());
                }
                if (event.kind() == ENTRY_MODIFY) {
                    System.out.println("Modify: " + event.context().toString());
                }
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.toString());
        }
    }
}
