package sync;

import callback.CompareCallback;
import callback.DownloadCallback;
import callback.HTTPCallback;
import objects.FileObject;
import org.json.JSONObject;
import requests.HTTPDownload;
import requests.HTTPGet;
import requests.HTTPUpload;
import utils.Config;
import utils.Parameters;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.*;

public class SyncHandler implements Runnable {

    private final byte[] receiveData = new byte[1024];
    private final byte[] sendData = new byte[1024];
    private Config config;
    private String json;
    private String IP;
    private String pass;
    private String dir;

    private List<String> files = new ArrayList<>();
    private DatagramSocket clientSocket;

    public SyncHandler(String json) {
        this.config = new Config();
        this.json = json;
        this.IP = config.getString("IP");
        this.pass = config.getString("pass");
        this.dir = config.getString("dir");

        try {
            clientSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        startSocket();
        sendMessage("Ping: " + config.getString("UUID"));
        new Compare(config, json).compareData(new CompareCallback() {
            @Override
            public void onComplete(List<FileObject> filesDownload, List<FileObject> filesUpload) {
                if (!filesDownload.isEmpty() || !filesUpload.isEmpty()) {
                    for (FileObject fileObject : filesDownload) {
                        // Download file
                        downloadFile(fileObject);
                    }

                    for (FileObject fileObject : filesUpload) {
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

        // Watch dir for changes
        Path path = Paths.get(dir);

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
                    Path fullPath = dir.resolve((Path) event.context());
                    File file = fullPath.toFile();
                    FileObject fileObject = new FileObject(fixPath(fullPath.toString()), file.isDirectory(), file.lastModified());

                    if (!file.getName().endsWith(".otak")) {
                        if (!files.contains(fileObject.file)) {
                            if (event.kind() == ENTRY_CREATE) {
                                if (fileObject.isDir) {
                                    // Register to listener
                                    fullPath.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                                    updateFile(fileObject, "dir");
                                } else {
                                    uploadFile(fileObject);
                                }
                            } else if (event.kind() == ENTRY_MODIFY) {
                                // Upload file if it isn't a directory. Check if file exists to prevent error on dir delete
                                if (file.isDirectory()) {
                                    fullPath.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                                } else {
                                    if (file.exists()) {
                                        uploadFile(fileObject);
                                    }
                                }
                            } else if (event.kind() == ENTRY_DELETE) {
                                updateFile(fixPath(fullPath.toString()), "delete");
                            }
                        }
                    } else {
                        files.remove(fileObject.file);
                    }
                }

                if (!watchKey.reset()) {
                    break;
                }
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.toString());
        }
    }

    /**
     * Downloads file from Otak Server if the file is not a directory.
     * If it is a directory it will be created.
     */
    private void downloadFile(FileObject fileObject) {
        files.add(fileObject.file);

        if (fileObject.isDir) {
            File file = new File(dir + File.separator + fileObject.file);
            file.mkdirs();

            // Set last modified to correct timestamp
            file.setLastModified(fileObject.timestamp);
        } else {
            File file = new File(dir + File.separator + fileObject.file + ".otak");

            Parameters parameters = new Parameters();
            parameters.add("pass", config.getString("pass"));
            parameters.add("file", fileObject.file);
            parameters.add("sender", config.getString("UUID"));

            new HTTPDownload(IP + "/download", parameters.toString()).downloadFile(file, new DownloadCallback() {
                @Override
                public void onRequestComplete() {
                    // Set last modified to correct timestamp
                    file.setLastModified(fileObject.timestamp);

                    System.out.println(file.getPath().replace(".otak", ""));
                    File newFile = new File(file.getPath().replace(".otak", ""));
                    file.renameTo(newFile);
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
        File file = new File(dir + File.separator + fileObject.file);

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
     * Creates directories or deletes files on the server
     *
     * @param file file path
     * @param type can be 'dir' or 'delete'
     */
    private void updateFile(String file, String type) {
        Parameters parameters = new Parameters();
        parameters.add("pass", config.getString("pass"));
        parameters.add("file", file);
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
                            File file = new File(config.getString("dir") + File.separator + json.getString("file"));

                            if (!file.exists()) {
                                downloadFile(new FileObject(json.getString("file"), json.getBoolean("dir"), json.getLong("timestamp")));
                            }
                        }

                        if (update.startsWith("Delete: ")) {
                            json = new JSONObject(update.replace("Delete: ", ""));

                            File file = new File(config.getString("dir") + File.separator + json.getString("file"));
                            file.delete();
                        }

                        if (update.startsWith("Modified: ")) {
                            json = new JSONObject(update.replace("Modified: ", ""));

                            downloadFile(new FileObject(json.getString("file"), json.getBoolean("dir"), json.getLong("timestamp")));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void sendMessage(String message) {
        try {
            URL url = new URL(IP);
            InetAddress address = InetAddress.getByName(url.getHost());

            byte buff[] = message.getBytes();
            DatagramPacket packetSend = new DatagramPacket(buff, buff.length, address, url.getPort());

            clientSocket.send(packetSend);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
