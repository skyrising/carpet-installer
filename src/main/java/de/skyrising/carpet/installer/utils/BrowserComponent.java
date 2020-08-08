package de.skyrising.carpet.installer.utils;

import java.awt.*;
import java.util.function.Supplier;

public abstract class BrowserComponent {
    private static Supplier<BrowserComponent> provider;
    static {
        try {
            provider = JavaFXBrowserComponent::new;
        } catch (LinkageError e) {
            provider = SwingBrowserComponent::new;
        }
    }

    public static BrowserComponent create() {
        return provider.get();
    }

    public abstract void setContentType(String mime);
    public abstract void setSource(String source);
    public abstract Component getComponent();
}
