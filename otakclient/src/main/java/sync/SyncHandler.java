package sync;

import callbacks.CompareCallback;
import callbacks.DownloadCallback;
import callbacks.HTTPCallback;
import com.zeshanaslam.otak.Main;
import objects.FileObject;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.json.JSONObject;
import requests.HTTPDownload;
import requests.HTTPGet;
import requests.HTTPUpload;
import utils.Config;
import utils.Parameters;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class SyncHandler implements Runnable {

    private Config config;
    private String json;
    private DatagramSocket clientSocket;

    private List<String> filesIgnore = new ArrayList<>();
    private String marker = ".DNUPLOAD";

    public SyncHandler(String json) {
        this.config = Main.config;
        this.json = json;

        // Initialize socket
        try {
            clientSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        // Start socket
        startSocket();

        // Send server client UUID
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("UUID", config.getString("UUID"));
        sendMessage("Ping: " + jsonObject);

        new Compare(json).compareData(new CompareCallback() {
            @Override
            public void onComplete(List<FileObject> filesDownload, List<FileObject> filesUpload) {
                if (!filesDownload.isEmpty() || !filesUpload.isEmpty()) {
                    for (FileObject fileObject : filesDownload) {
                        // Download file
                        downloadFile(fileObject);
                    }

                    for (FileObject fileObject : filesUpload) {
                        if (fileObject.file.contains(marker)) {
                            continue;
                        }

                        if (fileObject.isDir) {
                            // Create dir on server
                            updateFile(fileObject, "dir");
                        } else {
                            // Upload file to server
                            uploadFile(fileObject);
                        }
                    }

                    int sFiles = filesDownload.size() + filesUpload.size();
                    System.out.println("Synced " + sFiles + " files!");
                } else {
                    System.out.println("No sync needed!");
                }
            }
        });

        // Watch directory for changes
        FileAlterationObserver fileAlterationObserver = new FileAlterationObserver(config.getString("dir"));
        fileAlterationObserver.addListener(new FileAlterationListener() {
            @Override
            public void onStart(FileAlterationObserver fileAlterationObserver) {

            }

            @Override
            public void onDirectoryCreate(File file) {
                FileObject fileObject = new FileObject(fixPath(file.getAbsolutePath()), true, 0);

                if (!filesIgnore.contains(fileObject.file)) {
                    updateFile(fileObject, "dir");
                } else {
                    filesIgnore.remove(fileObject.file);
                }
            }

            @Override
            public void onDirectoryChange(File file) {
                // Ignore
            }

            @Override
            public void onDirectoryDelete(File file) {
                FileObject fileObject = new FileObject(fixPath(file.getAbsolutePath()), true, 0);

                updateFile(fileObject, "delete");
            }

            @Override
            public void onFileCreate(File file) {
                FileObject fileObject = new FileObject(fixPath(file.getAbsolutePath()), file.isDirectory(), file.lastModified());

                if (!fileObject.file.endsWith(marker)) {
                    if (!filesIgnore.contains(fileObject.file)) {
                        uploadFile(fileObject);
                    } else {
                        filesIgnore.remove(fileObject.file);
                    }
                }
            }

            @Override
            public void onFileChange(File file) {
                FileObject fileObject = new FileObject(fixPath(file.getAbsolutePath()), file.isDirectory(), file.lastModified());

                if (!fileObject.file.endsWith(marker)) {
                    if (!filesIgnore.contains(fileObject.file)) {
                        uploadFile(fileObject);
                    } else {
                        filesIgnore.remove(fileObject.file);
                    }
                }
            }

            @Override
            public void onFileDelete(File file) {
                FileObject fileObject = new FileObject(fixPath(file.getAbsolutePath()), false, 0);

                if (!fileObject.file.endsWith(marker)) {
                    if (!filesIgnore.contains(fileObject.file)) {
                        updateFile(fileObject, "delete");
                    } else {
                        filesIgnore.remove(fileObject.file);
                    }
                }
            }

            @Override
            public void onStop(FileAlterationObserver fileAlterationObserver) {

            }
        });

        FileAlterationMonitor monitor = new FileAlterationMonitor(3000);
        monitor.addObserver(fileAlterationObserver);

        try {
            monitor.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Start socket connection to server to be notified of updates
     */
    private void startSocket() {
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

                            downloadFile(new FileObject(json.getString("file"), json.getBoolean("dir"), json.getLong("timestamp")));
                        }

                        if (update.startsWith("Delete: ")) {
                            json = new JSONObject(update.replace("Delete: ", ""));
                            System.out.println("Delete: " + json);

                            File file = new File(config.getString("dir") + File.separator + json.getString("file"));
                            file.delete();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
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

    /**
     * Downloads file from Otak Server if the file is not a directory.
     * If it is a directory it will be created.
     */
    private void downloadFile(FileObject fileObject) {
        filesIgnore.add(fileObject.file);

        if (fileObject.isDir) {
            File file = new File(config.getString("dir") + File.separator + fileObject.file);
            file.mkdirs();

            // Set last modified to correct timestamp
            file.setLastModified(fileObject.timestamp);
        } else {
            File file = new File(config.getString("dir") + File.separator + fileObject.file + marker);

            Parameters parameters = new Parameters();
            parameters.add("pass", config.getString("pass"));
            parameters.add("file", fileObject.file);
            parameters.add("sender", config.getString("UUID"));

            new HTTPDownload(config.getString("IP") + "/download", parameters.toString()).downloadFile(file, new DownloadCallback() {
                @Override
                public void onRequestComplete() {
                    // Set last modified to correct timestamp
                    file.setLastModified(fileObject.timestamp);

                    // Rename file
                    renameFile(file, fileObject.file);
                }

                @Override
                public void onProgress(int progress) {

                }

                @Override
                public void onRequestFailed() {
                    // Add to queue and try again later
                }
            });
        }
    }

    /**
     * Uploads file to te Otak server
     */
    private void uploadFile(FileObject fileObject) {
        File file = new File(config.getString("dir") + File.separator + fileObject.file);

        Parameters parameters = new Parameters();
        parameters.add("pass", config.getString("pass"));
        parameters.add("file", fileObject.file);
        parameters.add("type", "file");
        parameters.add("sender", config.getString("UUID"));

        new HTTPUpload(config.getString("IP") + "/upload", parameters.toString()).sendPost(file, new HTTPCallback() {
            @Override
            public void onSuccess(String IP, String response) {

            }

            @Override
            public void onError() {
                // Add to queue and try again later
            }
        });
    }

    /**
     * Creates directories or deletes files on the server
     *
     * @param type can be 'dir' or 'delete'
     */
    private void updateFile(FileObject fileObject, String type) {

        Parameters parameters = new Parameters();
        parameters.add("pass", config.getString("pass"));
        parameters.add("file", fileObject.file);
        parameters.add("type", type);
        parameters.add("sender", config.getString("UUID"));

        new HTTPGet(config.getString("IP") + "/upload", parameters.toString()).sendGet(new HTTPCallback() {
            @Override
            public void onSuccess(String IP, String response) {

            }

            @Override
            public void onError() {
                // Add to queue and try again later
            }
        });
    }

    /**
     * Fix string to stay consistent to server path
     */
    private String fixPath(String path) {
        return path.replace(config.getString("dir"), "").replaceAll("\\\\", "/");
    }


    /**
     * Rename file
     */
    private void renameFile(File file, String name) {
        Path oldFile = file.toPath();
        Path newFile = Paths.get(config.getString("dir") + File.separator + name);

        try {
            Files.move(oldFile, newFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}