package controllers;

import callback.HTTPCallback;
import callback.OtakServerFoundCallback;
import javafx.application.Platform;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import listeners.JmDNSListener;
import objects.ServerObject;
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
import views.HomeView;

import javax.jmdns.JmDNS;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.UUID;

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
        webView.setContextMenuEnabled(false);
        webView.getEngine().load("file:///" + Paths.get(".").toAbsolutePath().normalize().toString() + File.separator + "data" + File.separator + "index.html");

        // Make web view responsive
        new ResponsiveWeb(anchorPane, webView).makeResponsive();

        // Wait for UI to finish loading
        webView.getEngine().getLoadWorker().stateProperty().addListener((observableValue, oldState, newState) -> {
            if (newState == State.SUCCEEDED) {
                final Document doc = webView.getEngine().getDocument();

                // Directory selection
                final HTMLInputElement input = (HTMLInputElement) doc.getElementById("input_install");
                ((EventTarget) input).addEventListener("click", evt -> {
                    DirectoryChooser dirChooser = new DirectoryChooser();
                    dirChooser.setTitle("Select Otak Installation Directory");

                    File file = dirChooser.showDialog(stage);
                    if (file != null) {
                        input.setAttribute("value", file.getPath());
                    }
                }, false);

                final Element installButton = doc.getElementById("btn_install");
                ((EventTarget) installButton).addEventListener("click", evt -> {
                    if (input.getValue() == null || !new File(input.getValue()).exists()) {
                        DirectoryChooser dirChooser = new DirectoryChooser();
                        dirChooser.setTitle("Select Otak Installation Directory");

                        File file = dirChooser.showDialog(stage);
                        if (file != null) {
                            input.setAttribute("value", file.getPath());
                        }
                    } else {
                        config.set("dir", input.getValue());
                        runScript("installDone();");
                    }
                }, false);

                // Directory selection end

                // Search for otak servers on local network
                final HashMap<String, ServerObject> serverObjectHashMap = new HashMap<>();
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            JmDNS jmdns = JmDNS.create();
                            jmdns.addServiceListener("_http.otak._tcp.local.", new JmDNSListener(new OtakServerFoundCallback() {

                                @Override
                                public void onFound(final JmDNS jmDNS, final ServerObject serverObject) {

                                    // Add found Otak servers to  UI list
                                    Platform.runLater(() -> {
                                        serverObjectHashMap.put(serverObject.name, serverObject);
                                        runScript("addDomain(\"" + serverObject.name + "\");");
                                    });
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
                                    Platform.runLater(() -> {
                                        serverObjectHashMap.put(serverObject.name, serverObject);
                                        runScript("addDomain(\"" + serverObject.name + "\");");
                                    });
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

                // IP connection
                final Element buttonIP = doc.getElementById("btn_ip");
                final HTMLInputElement inputIP = (HTMLInputElement) doc.getElementById("input_IP");
                ((EventTarget) buttonIP).addEventListener("click", evt -> {
                    String serverIP = inputIP.getValue();

                    if (serverIP.startsWith("Local: ")) {
                        serverIP = serverIP.replace("Local: ", "");

                        if (serverObjectHashMap.containsKey(serverIP)) {
                            serverIP = serverObjectHashMap.get(serverIP).IP;
                        }
                    }

                    new HTTPGet(serverIP).sendGet(new HTTPCallback() {
                        @Override
                        public void onSuccess(String IP, String response) {
                            config.set("IP", IP);
                            runScript("$('#myModal').modal('toggle');");
                        }

                        @Override
                        public void onError() {
                            runScript("addNotification('error-server');");
                        }
                    });
                }, false);

                // Connection password verify
                final Element buttonPass = doc.getElementById("button_login");
                final HTMLInputElement inputPass = (HTMLInputElement) doc.getElementById("input_pass");
                ((EventTarget) buttonPass).addEventListener("click", evt -> {
                    final String password = inputPass.getValue();
                    final String IP = config.getString("IP");

                    Parameters parameters = new Parameters();
                    parameters.add("pass", password);

                    new HTTPGet(IP + "/list", parameters.toString()).sendGet(new HTTPCallback() {
                        @Override
                        public void onSuccess(String IP, String response) {
                            JSONObject jsonObject = new JSONObject(response);

                            if (jsonObject.getBoolean("success")) {
                                config.set("pass", password);
                                config.set("UUID", UUID.randomUUID());
                                config.set("setup", true);
                                config.save();

                                runScript("addNotification('connected');");

                                // Change stage to home page
                                Platform.runLater(() -> {
                                    stage.close();
                                    try {
                                        new HomeView().start(new Stage());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                });
                            } else {
                                runScript("addNotification('error-login');");
                            }
                        }

                        @Override
                        public void onError() {
                            runScript("addNotification('error-login');");
                        }
                    });
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

