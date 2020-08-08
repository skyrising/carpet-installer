package de.skyrising.carpet.installer;

import javax.swing.*;

public interface Handler {
    JPanel makePanel(InstallerGui gui);
    String getName();
}
