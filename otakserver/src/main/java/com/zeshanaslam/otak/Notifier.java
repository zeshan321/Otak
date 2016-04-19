package com.zeshanaslam.otak;

import Objects.ClientObject;
import utils.Config;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.*;

public class Notifier implements Runnable {

    private final byte[] receiveData = new byte[1024];
    private final byte[] sendData = new byte[1024];
    private DatagramSocket serverSocket;
    private HashMap<String, ClientObject> clients = new HashMap<>();
    private Config config;
    private int port;

    public Notifier(Config config, int port) {
        this.config = config;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            serverSocket = new DatagramSocket(port);

            while (true) {
                byte buffer[] = new byte[128];
                DatagramPacket packet =
                        new DatagramPacket(buffer, buffer.length);
                serverSocket.receive(packet);

                String UUID = new String(packet.getData()).replace("Hello: ", "");
                clients.put(UUID, new ClientObject(packet.getAddress(), packet.getPort()));
                sendMessage(clients.get(UUID), "How are you, " + UUID + "?");
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendMessage(ClientObject clientObject, String message) {
        try {
            DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), clientObject.IP, clientObject.port);

            serverSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startWatcher() {
        new Thread() {
            @Override
            public void run() {
                // Watch dir for changes
                Path path = Paths.get(config.getString("dir"));

                try {
                    final WatchService watcher = path.getFileSystem().newWatchService();

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

                            if (event.kind() == ENTRY_CREATE) {
                                if (file.isDirectory()) {
                                    // Register to listener
                                    path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

                                    // Notify about dir
                                } else {
                                    // Notify about file
                                }
                            } else if (event.kind() == ENTRY_MODIFY) {
                                // Upload file if it isn't a directory. Check if file exists to prevent error on dir delete
                                if (!file.isDirectory() && file.exists()) {
                                    // Notify about file
                                }
                            } else if (event.kind() == ENTRY_DELETE) {
                                // Notify about delete
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
        }.start();
    }
}
