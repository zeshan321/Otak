package com.zeshanaslam.otak;

import java.net.InetSocketAddress;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import com.sun.net.httpserver.HttpsServer;

import contexts.ConnectContext;
import secure.TLSHandler;

public class Server {
	
	private String IP;
	private int port;
	
	public Server(String IP, int port) {
		this.IP = IP;
		this.port = port;
	}
	
	public void start() {
		new Thread() {
			@Override
			public void run() {
				try {
					HttpsServer server = null;
					
					if (IP == null || IP.equalsIgnoreCase("localhost") || IP.equals("") || IP.equals(" ")) {
						server = HttpsServer.create(new InetSocketAddress(port), 0);
					} else {
						server = HttpsServer.create(new InetSocketAddress(IP, port), 0);
					}
					server.setHttpsConfigurator(new TLSHandler().createTLSContext());

					server.createContext("/", new ConnectContext());

					server.setExecutor(null);
					server.start();

					System.out.println("Status: Otak server is running!");
				} catch (Exception e) {
					System.out.println("Error: " + e.getMessage());
					e.printStackTrace();
				}
			}
		}.start();
		
		new Thread() {
			@Override
			public void run() {
				try {
					JmDNS mdnsServer = JmDNS.create();
					ServiceInfo testService = ServiceInfo.create("_http._tcp.local.", "Otak Server", port, "otak server");
					mdnsServer.registerService(testService);
				} catch (Exception e) {
					System.out.println("Error: " + e.getMessage());
					e.printStackTrace();
				}
			}
		}.start();
	}
}
