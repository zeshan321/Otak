package views;

import controllers.HomeController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import utils.SystemTraySupport;

import java.io.IOException;

public class HomeView extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

        initRootLayout();
    }

    private void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(this.getClass().getResource("/homelayout.fxml"));
            AnchorPane rootLayout = loader.load();

            // Get controller
            HomeController controller = loader.getController();
            controller.stage = primaryStage;

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setTitle("Otak Client");
            primaryStage.setScene(scene);
            primaryStage.show();

            // Add tray support
            new SystemTraySupport(primaryStage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
