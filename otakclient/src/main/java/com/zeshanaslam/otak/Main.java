package com.zeshanaslam.otak;

import utils.Config;
import views.HomeView;
import views.MainView;

public class Main {

    public static void main(String[] args) {
        new Thread() {
            @Override
            public void run() {
                // Fix javafx windows pixelated fonts
                System.setProperty("prism.lcdtext", "false");
                System.setProperty("prism.text", "t2k");

                // Start first view
                Config config = new Config();

                if (config.contains("setup") && config.getBoolean("setup")) {
                    javafx.application.Application.launch(HomeView.class);
                } else {
                    javafx.application.Application.launch(MainView.class);
                }
            }
        }.start();
    }
}