package de.skyrising.carpet.installer.utils;

import de.skyrising.carpet.installer.InstallerGui;
import de.skyrising.carpet.installer.Task;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;

public class ProgressDialog extends JDialog {
    private Task currentTask;
    private JLabel label;
    private JProgressBar bar;
    private Thread updateThread;
    private volatile boolean stopUpdateThread;

    public ProgressDialog(InstallerGui parent) {
        super(parent, true);
        this.setLayout(new BorderLayout());
        label = new JLabel("Installing");
        this.add(label, BorderLayout.PAGE_START);
        bar = new JProgressBar();
        this.add(bar, BorderLayout.CENTER);
        this.pack();
        this.setLocationRelativeTo(parent);
    }

    @Override
    public void show() {
        stopUpdateThread = false;
        updateThread = new Thread(() -> {
            while (!stopUpdateThread) {
                updateStatus();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {}
            }
        });
        updateThread.start();
        super.show();
    }

    @Override
    public void hide() {
        super.hide();
        stopUpdateThread = true;
        updateThread.interrupt();
    }

    public CompletableFuture<Boolean> runTask(Task task, boolean success) {
        if (!success) return CompletableFuture.completedFuture(false);
        currentTask = task;
        bar.setIndeterminate(!task.hasProgress());
        label.setText(task.getName());
        this.pack();
        this.setLocationRelativeTo(this.getParent());
        return CompletableFuture.supplyAsync(task::run);
    }

    public CompletableFuture<Boolean> runTask(String name, BooleanSupplier r, boolean success) {
        return runTask(new Task() {
            @Override
            public boolean run() {
                return r.getAsBoolean();
            }

            @Override
            public String getName() {
                return name;
            }
        }, success);
    }

    private void updateStatus() {
        if (currentTask == null) return;
        if (currentTask.hasProgress()) {
            bar.setValue((int)(currentTask.getProgress() * 100));
        }
    }
}
