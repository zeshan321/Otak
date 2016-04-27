package controllers;

import callback.HTTPCallback;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLInputElement;
import requests.HTTPGet;
import sync.SyncHandler;
import utils.Config;
import utils.Parameters;
import utils.ResponsiveWeb;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    public Stage stage;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private WebView webView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        final Config config = new Config();

        // Load site
        webView.getEngine().setJavaScriptEnabled(true);
        webView.setContextMenuEnabled(false);
        webView.getEngine().load("file:///" + Paths.get(".").toAbsolutePath().normalize().toString() + File.separator + "data" + File.separator + "home.html");

        // Make web view responsive
        new ResponsiveWeb(anchorPane, webView).makeResponsive();

        // Wait for UI to finish loading
        webView.getEngine().getLoadWorker().stateProperty().addListener((observableValue, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                Document doc = webView.getEngine().getDocument();
                // Start syncing files
                startSync(config);

                final Element settingsButton = doc.getElementById("menu-settings");
                ((EventTarget) settingsButton).addEventListener("click", evt -> {

                    // Server ip
                    HTMLInputElement input = (HTMLInputElement) doc.getElementById("serverIP");
                    input.setAttribute("value", config.getString("IP"));

                    // Password
                    input = (HTMLInputElement) doc.getElementById("password");
                    input.setAttribute("value", config.getString("pass"));

                    // Dir
                    input = (HTMLInputElement) doc.getElementById("dir");
                    input.setAttribute("value", config.getString("dir"));

                    // Auto sync
                    if (config.contains("sync")) {
                        runScript("$('#auto-sync').bootstrapToggle('on')");
                    } else {
                        runScript("$('#auto-sync').bootstrapToggle('off')");
                    }
                }, false);
            }
        });
    }

    private void runScript(String script) {
        Platform.runLater(() -> {
            webView.getEngine().executeScript(script);
        });
    }

    private void startSync(Config config) {
        Parameters parameters = new Parameters();
        parameters.add("pass", config.getString("pass"));

        new HTTPGet(config.getString("IP") + "/list", parameters.toString()).sendGet(new HTTPCallback() {
            @Override
            public void onSuccess(String IP, String response) {
                runScript("serverStatus('online');");

                JSONObject jsonObject = new JSONObject(response);

                if (jsonObject.getBoolean("success")) {
                    (new Thread(new SyncHandler(response))).start();
                } else {
                    System.out.println("Error");
                }
            }

            @Override
            public void onError() {
                runScript("serverStatus('offline');");
            }
        });
    }
}
