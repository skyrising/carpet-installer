package de.skyrising.carpet.installer;

import de.skyrising.carpet.installer.api.Api;

import javax.swing.*;

public class InstallerGui extends JFrame {
    public final Api api = new Api();

    private JTabbedPane contentPane;

    public InstallerGui() {
        contentPane = new JTabbedPane();
        for (Handler handler : Main.HANDLERS) contentPane.addTab(handler.getName(), handler.makePanel(this));
        setContentPane(contentPane);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public void start() throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        this.pack();
        this.setTitle("Carpet Mod Installer");
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
