package de.skyrising.carpet.installer.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.skyrising.carpet.installer.AbstractHandler;
import de.skyrising.carpet.installer.InstallerGui;
import de.skyrising.carpet.installer.api.Api;
import de.skyrising.carpet.installer.utils.DownloadTask;
import de.skyrising.carpet.installer.utils.FileChooserDialog;
import de.skyrising.carpet.installer.utils.ProgressDialog;
import de.skyrising.carpet.installer.utils.Utils;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class ServerHandler extends AbstractHandler {
    public ServerHandler() {
        super("Server");
    }

    @Override
    protected boolean isValidVersion(Api.VersionInfo version) {
        return version.hasServer;
    }

    @Override
    protected boolean isValidRelease(Api.ReleaseInfo release) {
        return true;
    }

    @Override
    protected void onInstallButton(InstallerGui gui, Api.VersionInfo version, Api.ReleaseInfo release) {
        ProgressDialog progress = new ProgressDialog(gui);
        SwingUtilities.invokeLater(() -> progress.setVisible(true));
        URL[] urls = new URL[2];
        File outputJar = Utils.<File>wrap(consumer -> FileChooserDialog.show(gui, "Select output JAR", true, consumer)).join();
        System.out.println(outputJar);
        try {
            urls[1] = new URL(release.downloads.get("server"));
            File serverJarTmp = File.createTempFile("server-", ".jar");
            serverJarTmp.deleteOnExit();
            File carpetJarTmp = File.createTempFile("carpet-", ".jar");
            carpetJarTmp.deleteOnExit();

            progress.runTask("Fetching metadata", () -> {
                try {
                    URLConnection conn = new URL(version.gameUrl).openConnection();
                    Gson gson = new Gson();
                    conn.connect();
                    JsonObject meta = gson.fromJson(new InputStreamReader(conn.getInputStream()), JsonObject.class);
                    urls[0] = new URL(meta.get("downloads").getAsJsonObject().get("server").getAsJsonObject().get("url").getAsString());
                    System.out.println(urls[0]);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }, true)
            .thenComposeAsync(downloadTask(progress, () -> urls[0], serverJarTmp))
            .thenComposeAsync(downloadTask(progress, () -> urls[1], carpetJarTmp))
            .thenComposeAsync(mergeTask(progress, outputJar, carpetJarTmp, serverJarTmp))
            .thenAccept(success -> SwingUtilities.invokeLater(() -> {
                progress.setVisible(false);
                if (success) {
                    JOptionPane.showMessageDialog(gui, "Done.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(gui, "An error occured", "Error", JOptionPane.ERROR_MESSAGE);
                }
            })).join();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //
    }

    @Override
    protected Api.VersionInfo getDefaultVersion(List<Api.VersionInfo> versions) {
        for (Api.VersionInfo v : versions) {
            if ("1.12.2".equals(v.version)) return v;
        }
        return versions.get(versions.size() - 1);
    }
}
