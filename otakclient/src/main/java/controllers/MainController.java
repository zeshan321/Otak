package controllers;

import callback.OtakServerFoundCallback;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import listeners.JmDNSListener;
import objects.ServerObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLInputElement;
import utils.Config;
import utils.ResponsiveWeb;

import javax.jmdns.JmDNS;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class MainController implements Initializable {

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
        webView.getEngine().load("file:///" + Paths.get(".").toAbsolutePath().normalize().toString() + File.separator + "data" + File.separator + "index.html");

        // Make web view responsive
        new ResponsiveWeb(anchorPane, webView).makeResponsive();

        // Wait for UI to finish loading
        webView.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
            @Override
            public void changed(ObservableValue observableValue, State oldState, State newState) {
                if (newState == State.SUCCEEDED) {
                    final Document doc = webView.getEngine().getDocument();

                    // Directory selection
                    final HTMLInputElement input = (HTMLInputElement) doc.getElementById("input_install");
                    ((EventTarget) input).addEventListener("click", new EventListener() {

                        @Override
                        public void handleEvent(Event evt) {
                            DirectoryChooser dirChooser = new DirectoryChooser();
                            dirChooser.setTitle("Select Otak Installation Directory");

                            File file = dirChooser.showDialog(stage);
                            if (file != null) {
                                input.setAttribute("value", file.getPath());
                            }
                        }

                    }, false);

                    final Element installButton = doc.getElementById("btn_install");
                    ((EventTarget) installButton).addEventListener("click", new EventListener() {

                        @Override
                        public void handleEvent(Event evt) {
                            if (input.getValue() == null || !new File(input.getValue()).exists()) {
                                DirectoryChooser dirChooser = new DirectoryChooser();
                                dirChooser.setTitle("Select Otak Installation Directory");

                                File file = dirChooser.showDialog(stage);
                                if (file != null) {
                                    input.setAttribute("value", file.getPath());
                                }
                            } else {
                                config.set("folder", input.getValue());
                                webView.getEngine().executeScript("installDone();");
                            }
                        }

                    }, false);

                    // Directory selection end

                    // Search for otak servers on local network
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                JmDNS jmdns = JmDNS.create();
                                jmdns.addServiceListener("_http._tcp.local.", new JmDNSListener(new OtakServerFoundCallback() {

                                    @Override
                                    public void onFound(final JmDNS jmDNS, final ServerObject serverObject) {

                                        // Add found Otak servers to  UI list
                                        Platform.runLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                webView.getEngine().executeScript("addDomain(\"" + serverObject.name + "\");");
                                            }
                                        });
                                    }

                                    @Override
                                    public void onRemove(final JmDNS jmDNS, final ServerObject serverObject) {

                                        // Remove if otak server is no longer online
                                        Platform.runLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                webView.getEngine().executeScript("removeDomain(\"" + serverObject.name + "\");");
                                            }
                                        });
                                    }
                                }));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();

                    // IP
                }
            }
        });
    }
}

