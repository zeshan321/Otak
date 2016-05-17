package controllers;

import callback.OtakServerFoundCallback;
import com.zeshanaslam.otak.Main;
import javafx.application.Platform;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import listeners.JmDNSListener;
import netscape.javascript.JSObject;
import objects.ServerObject;
import org.w3c.dom.Document;
import utils.Config;
import utils.ResponsiveWeb;

import javax.jmdns.JmDNS;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    public Stage stage;

    Config config;
    HashMap<String, ServerObject> serverObjectHashMap = new HashMap<>();

    @FXML
    private AnchorPane anchorPane;
    @FXML
    private WebView webView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        config = Main.config;

        // Load site
        webView.getEngine().setJavaScriptEnabled(true);
        webView.setContextMenuEnabled(false);
        webView.getEngine().load("file:///" + Paths.get(".").toAbsolutePath().normalize().toString() + File.separator + "data" + File.separator + "index.html");

        // Make web view responsive
        new ResponsiveWeb(anchorPane, webView).makeResponsive();

        // Wait for UI to finish loading
        webView.getEngine().getLoadWorker().stateProperty().addListener((observableValue, oldState, newState) -> {
            if (newState == State.SUCCEEDED) {
                Document doc = webView.getEngine().getDocument();
                JSObject window = (JSObject) webView.getEngine().executeScript("window");

                // Home handler
                window.setMember("setup", new MainHandler(this, doc));

                // Search for otak servers on local network
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            JmDNS jmdns = JmDNS.create();
                            jmdns.addServiceListener("_http.otak._tcp.local.", new JmDNSListener(new OtakServerFoundCallback() {

                                @Override
                                public void onFound(final JmDNS jmDNS, final ServerObject serverObject) {

                                    // Add found Otak servers to  UI list
                                    serverObjectHashMap.put(serverObject.name, serverObject);
                                    runScript("addDomain(\"" + serverObject.name + "\");");
                                }

                                @Override
                                public void onRemove(final JmDNS jmDNS, final ServerObject serverObject) {

                                    // Remove if otak server is no longer online
                                    runScript("removeDomain(\"" + serverObject.name + "\");");
                                }
                            }));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();

                new Thread() {
                    @Override
                    public void run() {
                        try {
                            JmDNS jmdns = JmDNS.create();
                            jmdns.addServiceListener("_https.otak._tcp.local.", new JmDNSListener(new OtakServerFoundCallback() {

                                @Override
                                public void onFound(final JmDNS jmDNS, final ServerObject serverObject) {

                                    // Add found Otak servers to  UI list
                                    serverObjectHashMap.put(serverObject.name, serverObject);
                                    runScript("addDomain(\"" + serverObject.name + "\");");
                                }

                                @Override
                                public void onRemove(final JmDNS jmDNS, final ServerObject serverObject) {
                                    // Remove if otak server is no longer online
                                    runScript("removeDomain(\"" + serverObject.name + "\");");
                                }
                            }));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });
    }

    void runScript(String script) {
        Platform.runLater(() -> webView.getEngine().executeScript(script));
    }
}

