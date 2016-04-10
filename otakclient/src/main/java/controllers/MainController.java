package controllers;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import org.w3c.dom.Document;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
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
                    final Document doc = webView.getEngine().getDocument();


                }
            }
        });
    }
}

