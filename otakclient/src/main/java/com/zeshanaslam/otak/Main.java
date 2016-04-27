package com.zeshanaslam.otak;

import javafx.application.Application;
import javafx.stage.Stage;
import utils.Config;
import views.HomeView;
import views.MainView;

public class Main extends Application {

    public static void main(String[] args) {
        new Thread() {
            @Override
            public void run() {
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

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.close();
    }
}