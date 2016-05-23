package com.zeshanaslam.otak;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;
import contexts.*;
import secure.TLSHandler;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Scanner;

class Server {

    private JmDNS jmDNS;
    private Scanner reader;
    private String IP;
    private String serverName;
    private boolean https;
    private int port;

    Server(String IP, String serverName, boolean https, int port) {
        reader = new Scanner(System.in);
        this.IP = IP;
        this.serverName = serverName;
        this.https = https;
        this.port = port;
    }

    void start() {
        if (https) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        HttpsServer server;

                        if (IP == null || IP.equalsIgnoreCase("localhost") || IP.equals("") || IP.equals(" ")) {
                            server = HttpsServer.create(new InetSocketAddress(port), 0);
                        } else {
                            server = HttpsServer.create(new InetSocketAddress(IP, port), 0);
                        }
                        server.setHttpsConfigurator(new TLSHandler().createTLSContext());

                        server.createContext("/", new ConnectContext());
                        server.createContext("/list", new ListContext());
                        server.createContext("/download", new DownloadContext());
                        server.createContext("/upload", new UploadContext());
                        server.createContext("/stream", new StreamContext());

                        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
                        server.start();

                        System.out.println("Status: Otak server is running!");

                        parseCommands();
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }.start();
        } else {
            new Thread() {
                @Override
                public void run() {
                    try {
                        HttpServer server;

                        if (IP == null || IP.equalsIgnoreCase("localhost") || IP.equals("") || IP.equals(" ")) {
                            server = HttpServer.create(new InetSocketAddress(port), 0);
                        } else {
                            server = HttpServer.create(new InetSocketAddress(IP, port), 0);
                        }

                        server.createContext("/", new ConnectContext());
                        server.createContext("/list", new ListContext());
                        server.createContext("/download", new DownloadContext());
                        server.createContext("/upload", new UploadContext());
                        server.createContext("/stream", new StreamContext());

                        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
                        server.start();

                        System.out.println("Status: Otak server is running!");

                        parseCommands();
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }.start();
        }

        new Thread() {
            @Override
            public void run() {
                try {
                    jmDNS = JmDNS.create();
                    ServiceInfo serviceInfo;

                    if (https) {
                        serviceInfo = ServiceInfo.create("_https.otak._tcp.local.", "Otak Server", port, serverName);
                    } else {
                        serviceInfo = ServiceInfo.create("_http.otak._tcp.local.", "Otak Server", port, serverName);
                    }

                    jmDNS.registerService(serviceInfo);
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void parseCommands() throws IOException {
        while (true) {
            System.out.print(" > ");
            String cmd = reader.nextLine();

            if (cmd.equals("exit")) {
                System.out.println("Exiting...");
                jmDNS.unregisterAllServices();
                jmDNS.close();
                System.exit(0);
            }

            System.out.println("Error: Unknown command");
        }
    }
}
