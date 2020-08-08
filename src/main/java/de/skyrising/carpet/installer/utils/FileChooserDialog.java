package de.skyrising.carpet.installer.utils;

import de.skyrising.carpet.installer.InstallerGui;
import javafx.application.Platform;
import javafx.stage.FileChooser;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;

public class FileChooserDialog {
    private static boolean hasJFX;

    static {
        try {
            Class.forName(FileChooser.class.getName());
            hasJFX = true;
        } catch (ClassNotFoundException e) {
            hasJFX = false;
        }
    }

    public static void show(InstallerGui parent, String title, boolean save, Consumer<File> onChosen) {
        if (hasJFX) showJFX(parent, title, save, onChosen);
        else showSwing(parent, title, save, onChosen);
    }

    private static void showSwing(InstallerGui parent, String title, boolean save, Consumer<File> onChosen) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(title);
        SwingUtilities.invokeLater(() -> {
            int ret = save ? chooser.showSaveDialog(parent) : chooser.showOpenDialog(parent);
            if (ret == JFileChooser.APPROVE_OPTION) {
                onChosen.accept(chooser.getSelectedFile());
            } else {
                onChosen.accept(null);
            }
        });
    }

    private static void showJFX(InstallerGui parent, String title, boolean save, Consumer<File> onChosen) {
        Platform.runLater(() -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle(title);
            Dialog d = new Dialog(parent, true);
            modalShow(d);
            onChosen.accept(save ? chooser.showSaveDialog(null) : chooser.showOpenDialog(null));
            modalHide(d);
        });
    }

    private static void modalShow(Dialog d) {
        try {
            Method modalShow = d.getClass().getDeclaredMethod("modalShow");
            modalShow.setAccessible(true);
            modalShow.invoke(d);
        } catch (ReflectiveOperationException ignored) {
            ignored.printStackTrace();
        }
    }

    private static void modalHide(Dialog d) {
        try {
            Method modalHide = d.getClass().getDeclaredMethod("modalHide");
            modalHide.setAccessible(true);
            modalHide.invoke(d);
        } catch (ReflectiveOperationException ignored) {
            ignored.printStackTrace();}
    }
}
