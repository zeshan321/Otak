package com.zeshanaslam.otak;

import javafx.application.Application;
import javafx.stage.Stage;

import listeners.JmDNSListener;
import views.MainView;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.InetAddress;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
    }

    public static void main(String[] args) {
        new Thread() {
            @Override
            public void run() {
                try {
                    JmDNS jmdns = JmDNS.create();
                    jmdns.addServiceListener("_http._tcp.local.", new JmDNSListener());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        new Thread() {
            @Override
            public void run() {
                // Fix javafx windows pixelated fonts
                System.setProperty("prism.lcdtext", "false");
                System.setProperty("prism.text", "t2k");

                // Start first view
                javafx.application.Application.launch(MainView.class);
            }
        }.start();
    }
}