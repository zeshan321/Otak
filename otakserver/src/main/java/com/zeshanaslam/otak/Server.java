package com.zeshanaslam.otak;

import com.sun.net.httpserver.HttpsServer;
import contexts.ConnectContext;
import secure.TLSHandler;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.net.InetSocketAddress;
import java.util.Scanner;

class Server {

    private JmDNS jmDNS;
    private String IP;
    private String serverName;
    private int port;

    Server(String IP, String serverName, int port) {
        this.IP = IP;
        this.serverName = serverName;
        this.port = port;
    }

    void start() {
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

                    server.setExecutor(null);
                    server.start();

                    System.out.println("Status: Otak server is running!");

                    Scanner reader = new Scanner(System.in);
                    while(true) {
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
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }.start();

        new Thread() {
            @Override
            public void run() {
                try {
                    jmDNS = JmDNS.create();
                    ServiceInfo testService = ServiceInfo.create("_http._tcp.local.", "Otak Server", port, serverName);
                    jmDNS.registerService(testService);
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
