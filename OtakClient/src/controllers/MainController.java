package controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
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
	}
}

