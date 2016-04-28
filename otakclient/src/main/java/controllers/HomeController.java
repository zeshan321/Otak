package controllers;

import com.zeshanaslam.otak.Main;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLInputElement;
import utils.Config;
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

    private Config config;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        config = Main.config;

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

                // Settings
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
                    input = (HTMLInputElement) doc.getElementById("auto-sync");
                    input.setChecked(config.getBoolean("sync"));
                }, false);

                final HTMLInputElement dirInput = (HTMLInputElement) doc.getElementById("dir");
                ((EventTarget) dirInput).addEventListener("click", evt -> {
                    DirectoryChooser dirChooser = new DirectoryChooser();
                    dirChooser.setTitle("Select Otak Installation Directory");

                    File file = dirChooser.showDialog(stage);
                    if (file != null) {
                        dirInput.setAttribute("value", file.getPath());
                    }
                }, false);

                final Element saveButton = doc.getElementById("btn-save");
                ((EventTarget) saveButton).addEventListener("click", evt -> {

                    // Server ip
                    HTMLInputElement input = (HTMLInputElement) doc.getElementById("serverIP");
                    config.set("IP", input.getValue());

                    // Password
                    input = (HTMLInputElement) doc.getElementById("password");
                    config.set("pass", input.getValue());

                    // Dir
                    input = (HTMLInputElement) doc.getElementById("dir");
                    config.set("dir", input.getValue());

                    // Auto sync
                    input = (HTMLInputElement) doc.getElementById("auto-sync");
                    config.set("sync", input.getChecked());

                    config.save();
                }, false);
            }
        });
    }

    private void runScript(String script) {
        Platform.runLater(() -> {
            webView.getEngine().executeScript(script);
        });
    }
}
