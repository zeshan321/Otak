package utils;


import javafx.application.Platform;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

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
            java.awt.Image image = null;
            try {
                URL url = new URL("http://images.all-free-download.com/images/graphiclarge/letter_o_pink_97067.jpg");
                image = ImageIO.read(url);
            } catch (IOException ex) {
                System.out.println(ex);
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
