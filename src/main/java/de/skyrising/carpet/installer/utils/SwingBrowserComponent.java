package de.skyrising.carpet.installer.utils;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;

public class SwingBrowserComponent extends BrowserComponent {
    private JEditorPane pane = new JEditorPane();

    public SwingBrowserComponent() {
        pane.setEditable(false);
        pane.setContentType("text/html");
        pane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED && Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(e.getURL().toURI());
                } catch (IOException | URISyntaxException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    @Override
    public void setContentType(String mime) {
        pane.setContentType(mime);
    }

    @Override
    public void setSource(String source) {
        pane.setText(source);
    }

    @Override
    public Component getComponent() {
        return pane;
    }
}
