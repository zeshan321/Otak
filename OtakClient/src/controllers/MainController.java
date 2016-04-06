package controllers;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLInputElement;

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

							HTTPGet httpGet = new HTTPGet(ipInput);
							String getResponse = httpGet.sendGet();

							if (getResponse == null) {
								webView.getEngine().executeScript("addNotification('unable-ip');");
								return;
							}
							
							if (getResponse.equals("Welcome to Otak")) {
								webView.getEngine().executeScript("addNotification('connected');");

								config.set("ip", ipInput);
								config.save();
							} else {
								webView.getEngine().executeScript("addNotification('unable-ip');");
							}
						}
					}, false);
				}
			}
		});
	}
}

