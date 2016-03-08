package com.zeshanaslam.otak;

import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpsServer;

import contexts.ConnectContext;
import secure.TLSHandler;

public class Main {
	
	public static void main(String args[]) throws Exception {
		HttpsServer server = HttpsServer.create(new InetSocketAddress(8000), 0);
		server.setHttpsConfigurator(new TLSHandler().createTLSContext());
		
		server.createContext("/", new ConnectContext());
		
		server.setExecutor(null);
		server.start();
		
		System.out.println("Otak server is running!");
	}
}
