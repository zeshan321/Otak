package controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.security.KeyStore;
import java.util.ResourceBundle;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLInputElement;

import com.zeshanaslam.otak.Main;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import secure.JKSGenerator;
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
		webView.getEngine().load("file:///C:/Users/zesha/Desktop/Otak/Templates/Server/index.html");

		// Make web view responsive
		new ResponsiveWeb(anchorPane, webView).makeResponsive();

		webView.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
			@Override 
			public void changed(ObservableValue observableValue, State oldState, State newState) {
				if (newState == State.SUCCEEDED) {
					Document doc = webView.getEngine().getDocument();

					Element buttonInstall = doc.getElementById("btn_install");
					Element buttonPass = doc.getElementById("btn_pass");
					HTMLInputElement input = (HTMLInputElement) doc.getElementById("input_install");


					((EventTarget) input).addEventListener("click", new EventListener() {

						@Override
						public void handleEvent(Event event) {	
							DirectoryChooser dirChooser = new DirectoryChooser();
							dirChooser.setTitle("Select Otak Installation Directory");

							File file = dirChooser.showDialog(stage);
							if (file != null) {
								input.setAttribute("value", file.getPath());
							}
						}
					}, false);

					((EventTarget) buttonInstall).addEventListener("click", new EventListener() {

						@Override
						public void handleEvent(Event event) {
							String dir = input.getAttribute("value");

							if (dir != null && new File(dir).exists()) {
								config.set("dir", input.getAttribute("value"));
								webView.getEngine().executeScript("addNotification('installing');");

								// Move files

								webView.getEngine().executeScript("installDone();");
							} else {
								webView.getEngine().executeScript("addNotification('invalid-dir');");
							}
						}
					}, false);

					((EventTarget) buttonPass).addEventListener("click", new EventListener() {

						@Override
						public void handleEvent(Event event) {
							String password = (String) webView.getEngine().executeScript("document.getElementById('input_password').value");


							if (password == null || password.length() < 5) {
								webView.getEngine().executeScript("addNotification('invalid-pass');");
								return;
							}

							webView.getEngine().executeScript("addNotification('encrypting');");
							
							config.set("pass", password);
							config.save();
							
							new JKSGenerator().generateKeyPair();
							webView.getEngine().executeScript("addNotification('server');");
							Main.startServer();
						}
					}, false);
				}
			}
		});
	}
}

