package controllers;

import javafx.scene.control.ContextMenu;
import javafx.stage.DirectoryChooser;
import objects.QueueObject;
import org.apache.tika.Tika;
import org.w3c.dom.Document;
import org.w3c.dom.html.HTMLInputElement;
import utils.Config;
import utils.PlayerSelect;
import views.StreamView;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class HomeHandler {

    private HomeController homeController;
    private Document doc;
    private Config config;

    public HomeHandler(HomeController homeController, Document doc) {
        this.homeController = homeController;
        this.doc = doc;
        this.config = homeController.config;
    }

    /**
     * Settings
     */
    public void settingsClick() {
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
    }

    public void dirChooser() {
        HTMLInputElement dirInput = (HTMLInputElement) doc.getElementById("dir");

        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Select Otak Installation Directory");

        File file = dirChooser.showDialog(homeController.stage);
        if (file != null) {
            dirInput.setAttribute("value", file.getPath());
        }
    }

    public void settingsSave() {
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
    }

    /**
     * Menu buttons
     */
    public void onHome() {
        String currentDir = homeController.currentDir;

        if (!currentDir.equals("")) {
            homeController.currentDir = "";
            homeController.parseMap();
        }
    }

    public void onBack() {
        String currentDir = homeController.currentDir;

        if (!currentDir.equals("")) {
            homeController.currentDir = currentDir.substring(0, currentDir.lastIndexOf("/"));
            homeController.parseMap();
        }
    }

    public void launchPlayer(String url, String player) {
        switch(player) {
            case "VLC":
                new PlayerSelect(url).startVLC();
                break;
            case "QuickTime":
                new PlayerSelect(url).startQuickTime();
                break;
            case "Otak":
                new StreamView(url).run();
                break;
            case "Other":

                break;
        }
    }

    /**
     * File click
     */

    /**
     * Runs on file clicks from UI
     *
     * @param click Mouse click type
     * @param loc   File location
     * @param name  File name
     * @param type  File extension
     */
    public void onClick(String click, String loc, String name, String type) {
        if (click.equals("left")) {
            homeController.contextMenu.hide();

            if (type.equals("folder")) {
                homeController.currentDir = loc;
                homeController.parseMap();
            } else {
                if (homeController.queueManager.contains(loc)) {
                    return;
                }

                File file = new File(config.getString("dir") + File.separator + loc);
                if (file.exists()) {
                    try {
                        Desktop.getDesktop().open(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }

                // Download file
                homeController.queueManager.add(loc, new QueueObject(QueueObject.QueueType.DOWNLOAD, file));
            }
        } else {
            homeController.contextMenu.hide();
            homeController.contextMenu = new ContextMenu();

            if (type.equals("folder")) {
                javafx.scene.control.MenuItem download = new javafx.scene.control.MenuItem("Download All");

                download.setOnAction(event -> {
                    java.util.List<String> files = new ArrayList<>();
                    java.util.List<String> dirs = new ArrayList<>();
                    dirs.add(loc);

                    homeController.recursiveFileDownload(files, dirs);
                });

                homeController.contextMenu.getItems().add(download);
            } else {
                String mime = new Tika().detect(loc);

                if (mime != null) {
                    if (mime.startsWith("video/") || mime.startsWith("audio/")) {
                        javafx.scene.control.MenuItem streamFile = new javafx.scene.control.MenuItem("Stream File");

                        streamFile.setOnAction(event -> {
                            homeController.playerSelect(loc, mime);
                        });

                        homeController.contextMenu.getItems().add(streamFile);
                    }
                }
            }

            javafx.scene.control.MenuItem menuItem = new javafx.scene.control.MenuItem("Delete");
            homeController.contextMenu.getItems().add(menuItem);

            homeController.contextMenu.show(homeController.webView, MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y);
        }
    }
}
