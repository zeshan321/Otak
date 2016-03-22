package com.zeshanaslam.otak;

import java.io.File;
import java.io.IOException;

import javafx.application.Application;
import javafx.stage.Stage;
import utils.ZipHandler;
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
            	
            	// Create temp files
            	String dir = System.getProperty("user.home") + File.separator + "OtakClient Files";
            	new File(dir).mkdir();
            	
            	ZipHandler zip = new ZipHandler(this.getClass().getResource("/install/Client.zip").getPath(), dir);
				try {
					zip.unzip();
				} catch (IOException e) {
					e.printStackTrace();
				}
            	
            	// Start first view
                javafx.application.Application.launch(MainView.class);
            }
        }.start();
	}
}