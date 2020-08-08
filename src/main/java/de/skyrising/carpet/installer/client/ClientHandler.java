package de.skyrising.carpet.installer.client;

import de.skyrising.carpet.installer.AbstractHandler;
import de.skyrising.carpet.installer.InstallerGui;
import de.skyrising.carpet.installer.api.Api;

import javax.swing.*;
import java.util.List;

public class ClientHandler extends AbstractHandler  {
    public ClientHandler() {
        super("Client");
    }

    @Override
    protected boolean isValidVersion(Api.VersionInfo version) {
        return version.hasClient;
    }

    @Override
    protected boolean isValidRelease(Api.ReleaseInfo release) {
        return true;
    }

    @Override
    protected void onInstallButton(InstallerGui gui, Api.VersionInfo version, Api.ReleaseInfo release) {
        JOptionPane.showMessageDialog(gui, "Client installation is not yet supported.", "Not supported", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    protected Api.VersionInfo getDefaultVersion(List<Api.VersionInfo> versions) {
        return versions.get(versions.size() - 1);
    }
}
