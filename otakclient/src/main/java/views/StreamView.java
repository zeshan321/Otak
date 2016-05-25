package views;


import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class StreamView {

    private String url;

    public StreamView(String url) {
        this.url = url;
    }

    public void run() {
        StackPane root = new StackPane();
        Stage stage = new Stage();
        Scene scene = new Scene(root, 600, 400);
        root.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

        Media pick = new Media(url);
        MediaPlayer player = new MediaPlayer(pick);
        player.play();

        // Output error
        player.setOnError(() -> System.out.println(player.getError().getMessage()));

        MediaView mediaView = new MediaView(player);
        root.getChildren().add(mediaView);

        // Resize
        DoubleProperty width = mediaView.fitWidthProperty();
        DoubleProperty height = mediaView.fitHeightProperty();

        width.bind(Bindings.selectDouble(mediaView.sceneProperty(), "width"));
        height.bind(Bindings.selectDouble(mediaView.sceneProperty(), "height"));

        mediaView.setPreserveRatio(true);

        // Exit
        stage.setOnCloseRequest(event -> {
            player.dispose();
            player.stop();
            stage.close();
        });

        stage.setTitle("Otak Player");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }
}
