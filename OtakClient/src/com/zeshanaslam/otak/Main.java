package com.zeshanaslam.otak;

import javafx.application.Application;
import javafx.stage.Stage;

import views.MainView;

public class Main extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {}

	public static void main(String[] args) {
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