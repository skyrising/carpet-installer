package de.skyrising.carpet.installer;

import de.skyrising.carpet.installer.client.ClientHandler;
import de.skyrising.carpet.installer.server.ServerHandler;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static final List<Handler> HANDLERS = Arrays.asList(new ServerHandler(), new ClientHandler());

    public static void main(String[] args) {
        if (args.length > 0) runCli(args);
        else runGui();
    }

    private static void runCli(String[] args) {

    }

    private static void runGui() {
        SwingUtilities.invokeLater(() -> {
            try {
                new InstallerGui().start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
