package utils;

import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;

public class ResponsiveWeb {

    private AnchorPane anchorPane;
    private WebView webView;

    public ResponsiveWeb(AnchorPane anchorPane, WebView webView) {
        this.anchorPane = anchorPane;
        this.webView = webView;
    }

    public void makeResponsive() {
        anchorPane.widthProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> {
            webView.setPrefWidth(newSceneWidth.doubleValue());
        });

        anchorPane.heightProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> {
            webView.setPrefHeight(newSceneWidth.doubleValue());
        });
    }
}
