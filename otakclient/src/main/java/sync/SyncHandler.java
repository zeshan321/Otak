package sync;

import callback.CompareCallback;
import callback.DownloadCallback;
import callback.HTTPCallback;
import objects.FileObject;
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
        sendMessage("Hello: " + config.getString("UUID"));
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

                    if (event.kind() == ENTRY_CREATE) {
                        if (fileObject.isDir) {
                            // Register to listener
                            path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                            updateFile(fileObject, "dir");
                        } else {
                            uploadFile(fileObject);
                        }
                    } else if (event.kind() == ENTRY_MODIFY) {
                        // Upload file if it isn't a directory. Check if file exists to prevent error on dir delete
                        if (!fileObject.isDir && file.exists()) {
                            uploadFile(fileObject);
                        }
                    } else if (event.kind() == ENTRY_DELETE) {
                        updateFile(fixPath(fullPath.toString()), "delete");
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
        File file = new File(dir + File.separator + fileObject.file);

        if (fileObject.isDir) {
            file.mkdirs();

            // Set last modified to correct timestamp
            file.setLastModified(fileObject.timestamp);
        } else {

            Parameters parameters = new Parameters();
            parameters.add("pass", config.getString("pass"));
            parameters.add("file", fileObject.file);

            new HTTPDownload(IP + "/download", parameters.toString()).downloadFile(file, new DownloadCallback() {
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

    private void startSocket() {
        new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
                        clientSocket.receive(packet);
                        System.out.println(new String(packet.getData()));
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
