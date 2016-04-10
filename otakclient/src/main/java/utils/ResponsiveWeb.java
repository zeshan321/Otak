package utils;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
        anchorPane.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
                webView.setPrefWidth(newSceneWidth.doubleValue());
            }
        });

        anchorPane.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
                webView.setPrefHeight(newSceneWidth.doubleValue());
            }
        });
    }
}
