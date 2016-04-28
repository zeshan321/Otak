package com.zeshanaslam.otak;

import javafx.application.Application;
import javafx.stage.Stage;
import utils.Config;
import views.HomeView;
import views.MainView;

public class Main extends Application {

    public static Config config = new Config();

    public static void main(String[] args) {
        // Fix javafx windows pixelated fonts
        if (System.getProperty("os.name").startsWith("Windows")) {
            System.setProperty("prism.lcdtext", "false");
            System.setProperty("prism.text", "t2k");
        }

        new Thread() {
            @Override
            public void run() {
                if (config.contains("setup") && config.getBoolean("setup")) {
                    javafx.application.Application.launch(HomeView.class);
                } else {
                    javafx.application.Application.launch(MainView.class);
                }
            }
        }.start();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.close();
    }
}