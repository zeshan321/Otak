package controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLInputElement;

import Listeners.JmDNSListener;
import callback.HTTPCallback;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import requests.HTTPGet;
import utils.Config;
import utils.ResponsiveWeb;

public class MainController implements Initializable {

	@FXML 
	private AnchorPane anchorPane;
	@FXML 
	private WebView webView;

	private Config config;
	public Stage stage;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		config = new Config();


		// Load site
		webView.getEngine().setJavaScriptEnabled(true);
		webView.getEngine().load("file:///" + Paths.get(".").toAbsolutePath().normalize().toString() + File.separator + "data" + File.separator + "index.html");

		// Make web view responsive
		new ResponsiveWeb(anchorPane, webView).makeResponsive();

		webView.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
			@Override 
			public void changed(ObservableValue observableValue, State oldState, State newState) {
				if (newState == State.SUCCEEDED) {
					Document doc = webView.getEngine().getDocument();

					// Auto detect server
					new Thread() {
						@Override
						public void run() {
							HTMLInputElement input = (HTMLInputElement) doc.getElementById("input_ip");

							try {
								JmDNS jmdns = JmDNS.create();
								jmdns.addServiceListener("_http._tcp.local.", new JmDNSListener());
								jmdns.close();
								
								ServiceInfo[] serviceInfoList = jmdns.list("_http._tcp.local.");
								for (int i = 0; i < serviceInfoList.length; i++) {
									System.out.println(serviceInfoList[i].getName());
									System.out.println(serviceInfoList[i].getURL());
									System.out.println("----------------------------");
								}								
							} catch(IOException e) {
								e.printStackTrace();
							}
						}
					}.start();

					Element buttonInstall = doc.getElementById("btn_install");
					Element buttonIP = doc.getElementById("btn_ip");
					HTMLInputElement input = (HTMLInputElement) doc.getElementById("input_install");


					((EventTarget) input).addEventListener("click", new EventListener() {

						@Override
						public void handleEvent(Event event) {	
							DirectoryChooser dirChooser = new DirectoryChooser();
							dirChooser.setTitle("Select Otak Directory");

							File file = dirChooser.showDialog(stage);
							if (file != null) {
								input.setAttribute("value", file.getPath());
							}
						}
					}, false);

					((EventTarget) buttonInstall).addEventListener("click", new EventListener() {

						@Override
						public void handleEvent(Event event) {	
							webView.getEngine().executeScript("installDone();");
						}
					}, false);

					((EventTarget) buttonIP).addEventListener("click", new EventListener() {

						@Override
						public void handleEvent(Event event) {
							HTMLInputElement input = (HTMLInputElement) doc.getElementById("input_ip");

							if (input.getValue() == null) {
								webView.getEngine().executeScript("addNotification('invalid-ip');");
								return;
							}

							String ipInput = input.getValue();
							if (!ipInput.startsWith("https://") || !ipInput.startsWith("HTTPS://")) {
								ipInput = "https://" + ipInput;
							}

							new HTTPGet(ipInput).sendGet(new HTTPCallback() {

								@Override
								public void onSuccess(String IP, String response) {
									if (response.equals("Welcome to Otak")) {
										webView.getEngine().executeScript("addNotification('connected');");

										config.set("ip", IP);
										config.save();
									} else {
										webView.getEngine().executeScript("addNotification('unable-ip');");
									}
								}

								@Override
								public void onError() {
									webView.getEngine().executeScript("addNotification('unable-ip');");
								}
							});
						}
					}, false);
				}
			}
		});
	}
}

