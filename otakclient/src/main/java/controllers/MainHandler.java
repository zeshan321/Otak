package controllers;

import callbacks.HTTPCallback;
import javafx.application.Platform;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.html.HTMLInputElement;
import requests.HTTPGet;
import utils.Config;
import utils.Parameters;
import views.HomeView;

import java.io.File;
import java.util.UUID;

public class MainHandler {

    private MainController mainController;
    private Document doc;
    private Config config;

    public MainHandler(MainController mainController, Document doc) {
        this.mainController = mainController;
        this.doc = doc;
        this.config = mainController.config;
    }

    /**
     * Directory page
     */

    public void onDirClick() {
        HTMLInputElement input = (HTMLInputElement) doc.getElementById("input_install");

        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Select Otak Installation Directory");

        File file = dirChooser.showDialog(mainController.stage);
        if (file != null) {
            input.setAttribute("value", file.getPath());
        }
    }

    public void onCompleteDirClick() {
        HTMLInputElement input = (HTMLInputElement) doc.getElementById("input_install");

        if (input.getValue() == null || !new File(input.getValue()).exists()) {
            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setTitle("Select Otak Installation Directory");

            File file = dirChooser.showDialog(mainController.stage);
            if (file != null) {
                input.setAttribute("value", file.getPath());
            }
        } else {
            config.set("dir", input.getValue());
            mainController.runScript("installDone();");
        }
    }

    /**
     * IP page
     */

    public void onIPClick() {
        HTMLInputElement inputIP = (HTMLInputElement) doc.getElementById("input_IP");

        String serverIP = inputIP.getValue();

        if (serverIP.startsWith("Local: ")) {
            serverIP = serverIP.replace("Local: ", "");

            if (mainController.serverObjectHashMap.containsKey(serverIP)) {
                serverIP = mainController.serverObjectHashMap.get(serverIP).IP;
            }
        }

        new HTTPGet(serverIP).sendGet(new HTTPCallback() {
            @Override
            public void onSuccess(String IP, String response) {
                JSONObject jsonObject = new JSONObject(response);

                config.set("IP", IP);
                config.set("name", jsonObject.getString("name"));

                mainController.runScript("$('#myModal').modal('toggle');");
            }

            @Override
            public void onError() {
                mainController.runScript("addNotification('error-server');");
            }
        });
    }

    /**
     * Verify user
     */

    public void verifyUser() {
        HTMLInputElement inputPass = (HTMLInputElement) doc.getElementById("input_pass");

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

                    mainController.runScript("addNotification('connected');");

                    // Change stage to home page
                    Platform.runLater(() -> {
                        mainController.stage.close();
                        try {
                            new HomeView().start(new Stage());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    mainController.runScript("addNotification('error-login');");
                }
            }

            @Override
            public void onError() {
                mainController.runScript("addNotification('error-login');");
            }
        });
    }
}
