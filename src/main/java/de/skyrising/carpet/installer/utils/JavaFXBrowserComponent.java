package de.skyrising.carpet.installer.utils;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLAnchorElement;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class JavaFXBrowserComponent extends BrowserComponent {
    private JFXPanel panel = new JFXPanel();
    private WebView webView;
    private String contentType = "text/html";

    public JavaFXBrowserComponent() {
        Platform.runLater(() -> {
            Group root = new Group();
            Scene scene = new Scene(root);
            webView = new WebView();
            WebEngine webEngine = webView.getEngine();
            webEngine.getLoadWorker().stateProperty().addListener((ov, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    NodeList as = webEngine.getDocument().getElementsByTagName("a");
                    for (int i = 0; i < as.getLength(); i++) {
                        ((EventTarget) as.item(i)).addEventListener("click", ev -> {
                            ev.preventDefault();
                            ev.stopPropagation();
                            String url = ((HTMLAnchorElement) ev.getTarget()).getHref();
                            SwingUtilities.invokeLater(() -> {
                                if (Desktop.isDesktopSupported()) {
                                    try {
                                        Desktop.getDesktop().browse(new URL(url).toURI());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }, false);
                    }
                }
            });
            root.getChildren().add(webView);
            panel.setScene(scene);
        });
    }

    @Override
    public void setContentType(String mime) {
        contentType = mime;
    }

    @Override
    public void setSource(String source) {
        Platform.runLater(() -> {
            webView.getEngine().loadContent(source, contentType);
        });
    }

    @Override
    public Component getComponent() {
        return panel;
    }
}
