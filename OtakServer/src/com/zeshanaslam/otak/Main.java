package com.zeshanaslam.otak;

import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpsServer;

import contexts.ConnectContext;
import secure.TLSHandler;
import views.MainView;

public class Main {

	public static void main(String args[]) throws Exception {
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

	public static void startServer() {
		new Thread() {
			@Override
			public void run() {
				try {
					HttpsServer server = HttpsServer.create(new InetSocketAddress(8000), 0);
					server.setHttpsConfigurator(new TLSHandler().createTLSContext());

					server.createContext("/", new ConnectContext());

					server.setExecutor(null);
					server.start();

					System.out.println("Otak server is running!");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
}
