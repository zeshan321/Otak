package controllers;

import java.net.URL;
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
import javafx.stage.Stage;
import requests.HTTPGet;
import utils.ResponsiveWeb;

public class MainController implements Initializable {

	@FXML private AnchorPane anchorPane;
	@FXML private WebView webView;

	public Stage stage;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Load site
		webView.getEngine().setJavaScriptEnabled(true);
		webView.getEngine().load("file:///C:/Users/zesha/Desktop/Otak/Templates/Client/index.html");

		// Make web view responsive
		new ResponsiveWeb(anchorPane, webView).makeResponsive();

		webView.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
			@Override 
			public void changed(ObservableValue observableValue, State oldState, State newState) {
				if (newState == State.SUCCEEDED) {
					Document doc = webView.getEngine().getDocument();
					Element button = doc.getElementById("btn_ip");
					((EventTarget) button).addEventListener("click", new EventListener() {

						@Override
						public void handleEvent(Event event) {
							HTMLInputElement input = (HTMLInputElement) doc.getElementById("input_ip");

							if (input.getValue() == null) {
								webView.getEngine().executeScript("addNotification('invalid-ip');");
								return;
							}

							webView.getEngine().executeScript("addNotification('loading');");

							// Add logic to check if able to connect
							String ipInput = input.getValue();
							if (!ipInput.startsWith("https://") || !ipInput.startsWith("HTTPS://")) {
								ipInput = "https://" + ipInput;
							}

							System.out.println(ipInput);
							HTTPGet httpGet = new HTTPGet(ipInput);
							String getResponse = httpGet.sendGet();

							System.out.println(getResponse);
							if (getResponse == null) {
								webView.getEngine().executeScript("addNotification('unable-ip');");
							} else {
								if (getResponse.equals("Welcome to Otak")) {
									System.out.println("1");
									webView.getEngine().executeScript("addNotification('connected');");
								} else {
									System.out.println("2");
									webView.getEngine().executeScript("addNotification('unable-ip');");
								}
							}
						}
					}, false);
				}
			}
		});
	}
}

