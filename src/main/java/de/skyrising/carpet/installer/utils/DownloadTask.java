package de.skyrising.carpet.installer.utils;

import de.skyrising.carpet.installer.Task;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadTask implements Task {
    public final URL url;
    private final OutputStream out;
    private volatile boolean cancelled;

    private long done;
    private long length;

    public DownloadTask(URL url, OutputStream out) {
        this.url = url;
        this.out = out;
    }

    @Override
    public boolean run() {
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            this.length = conn.getHeaderFieldLong("Content-Length", -1);
            this.done = 0;
            byte[] buf = new byte[8192];
            try (InputStream in = conn.getInputStream(); OutputStream out = this.out) {
                int read;
                while ((read = in.read(buf)) != -1) {
                    if (cancelled) break;
                    out.write(buf, 0, read);
                    done += read;
                    System.out.println(done + ", " + getProgress());
                }
            }
            return !cancelled;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String getName() {
        String url = this.url.toString();
        return "Downloading " + url.replace(this.url.getPath(), "/..." + url.substring(url.lastIndexOf('/')));
    }

    @Override
    public boolean hasProgress() {
        return length >= 0;
    }

    @Override
    public double getProgress() {
        return length == 0 ? 1 : (double) done / (double) length;
    }

    @Override
    public boolean cancel() {
        this.cancelled = true;
        return true;
    }
}
