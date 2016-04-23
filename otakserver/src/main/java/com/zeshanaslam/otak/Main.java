package com.zeshanaslam.otak;

import objects.ClientObject;
import org.json.JSONObject;
import secure.JKSGenerator;
import utils.Config;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Scanner;
import java.util.UUID;

public class Main {

    public static Config config;
    public static HashMap<String, ClientObject> clients = new HashMap<>();
    static DatagramSocket serverSocket;

    public static void main(String args[]) throws Exception {
        Scanner reader = new Scanner(System.in);

        // Load config
        config = new Config();

        if (!config.contains("setup")) {
            System.out.println("Welcome to Otak. Starting setup...");

            System.out.println("\nEnter a name for Otak:");
            System.out.print(" > ");
            config.set("name", reader.nextLine());

            System.out.println("\nEnter the IP you want Otak to run on:");
            System.out.println("Example: localhost");
            System.out.print(" > ");
            config.set("IP", reader.nextLine());

            System.out.println("\nEnter the port you want Otak to run on:");
            System.out.println("Example: 8000");
            System.out.print(" > ");
            config.set("port", reader.nextLine());

            System.out.println("\nEnter a secure password for encryption:");
            System.out.print(" > ");
            config.set("pass", reader.nextLine());

            System.out.println("\nSelect directory to save files:");
            System.out.print(" > ");
            config.set("dir", reader.nextLine());

            System.out.println("\nEnable HTTPS: (true or false)");
            System.out.print(" > ");
            config.set("https", reader.nextBoolean());

            System.out.println("\nGenerating UUID...");
            config.set("UUID", UUID.randomUUID());

            config.set("setup", true);
            System.out.println("\nSaving data...");
            config.save();

            System.out.println("\nGenerating keystore...");
            new JKSGenerator(config.getString("pass")).generateKeyPair();

            System.out.println("\nStarting server...");

            // Clear console
            System.out.print("\033[H\033[2J");
            System.out.flush();

            // Start servers
            startServers();
        } else {
            // Start servers
            startServers();
        }
    }

    public static void sendMessage(ClientObject clientObject, String message) {
        try {
            DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), clientObject.IP, clientObject.port);

            serverSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendMessage(String message, String UUID) {
        try {
            for (ClientObject clientObject : clients.values()) {
                if (!clientObject.UUID.equals(UUID)) {
                    DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), clientObject.IP, clientObject.port);

                    serverSocket.send(packet);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void startServers() {
        System.out.println("Welcome to Otak. Starting server: " + config.getString("name"));
        System.out.println("HTTPS: " + config.getString("https") + "\n");

        int port = config.getInt("port");

        // Start HTTP server
        new Server(config.getString("IP"), config.getString("name"), config.getBoolean("https"), port).start();

        // Start UDP socket
        try {
            serverSocket = new DatagramSocket(port);

            while (true) {
                byte buffer[] = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                serverSocket.receive(packet);

                JSONObject jsonObject = new JSONObject(new String(packet.getData()).replace("Ping: ", ""));
                String UUID = jsonObject.getString("UUID");

                clients.put(UUID, new ClientObject(UUID, packet.getAddress(), packet.getPort()));
                sendMessage(clients.get(UUID), "Pong: " + UUID);
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
