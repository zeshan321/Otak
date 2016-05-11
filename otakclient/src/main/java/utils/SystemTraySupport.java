package utils;

import javafx.application.Platform;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Paths;

public class SystemTraySupport {

    private Stage stage;

    public SystemTraySupport(Stage stage) {
        this.stage = stage;

        createTray();
    }

    private void createTray() {
        Platform.setImplicitExit(false);

        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            Image image;
            if (System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0) {
                image = Toolkit.getDefaultToolkit().getImage(Paths.get(".").toAbsolutePath().normalize().toString() + File.separator + "data" + File.separator + "images" + File.separator + "Otak-icon-b.png");
            } else {
                image = Toolkit.getDefaultToolkit().getImage(Paths.get(".").toAbsolutePath().normalize().toString() + File.separator + "data" + File.separator + "images" + File.separator + "Otak-icon.png");
            }


            stage.setOnCloseRequest(t -> hide());

            final ActionListener closeListener = e -> System.exit(0);

            final ActionListener showListener = e -> Platform.runLater(() -> {
                stage.show();
                stage.toFront();
                stage.setMaximized(true);
            });

            // create a popup menu
            PopupMenu popup = new PopupMenu();

            MenuItem showItem = new MenuItem("Show");
            showItem.addActionListener(showListener);
            popup.add(showItem);

            MenuItem closeItem = new MenuItem("Exit");
            closeItem.addActionListener(closeListener);
            popup.add(closeItem);

            TrayIcon trayIcon = new TrayIcon(image, "Otak Client", popup);
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(showListener);

            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.err.println(e);
            }
        }
    }

    private void hide() {
        Platform.runLater(() -> {
            if (SystemTray.isSupported()) {
                stage.hide();
            } else {
                System.exit(0);
            }
        });
    }
}
