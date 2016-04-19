package com.zeshanaslam.otak;

import secure.JKSGenerator;
import utils.Config;

import java.util.Scanner;
import java.util.UUID;

public class Main {

    public static Config config;

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

            // Close reader
            reader.close();

            System.out.println("\nGenerating UUID...");
            config.set("UUID", UUID.randomUUID());

            config.set("setup", true);
            System.out.println("\nSaving data...");
            config.save();

            System.out.println("\nGenerating keystore...");
            new JKSGenerator(config.getString("pass")).generateKeyPair();

            System.out.println("\nStarting server...");
            new Server(config.getString("IP"), config.getString("name"), config.getBoolean("https"), config.getInt("port")).start();
        } else {
            System.out.println("Welcome to Otak. Starting server: " + config.getString("name"));
            System.out.println("HTTPS: " + config.getString("https") + "\n");
            new Server(config.getString("IP"), config.getString("name"), config.getBoolean("https"), config.getInt("port")).start();
            new Thread(new Notifier(config, config.getInt("port"))).start();
        }
    }
}
