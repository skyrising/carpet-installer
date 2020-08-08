package de.skyrising.carpet.installer.utils;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.*;

public class ZipMerger {
    private final InputStream[] inputs;
    private final OutputStream output;

    public ZipMerger(OutputStream output, InputStream ...inputs) {
        this.inputs = inputs;
        this.output = output;
    }

    public void merge() throws IOException {
        Set<String> names = new HashSet<>();
        try (ZipOutputStream outputZip = new ZipOutputStream(output)) {
            for (InputStream input : inputs) {
                try (ZipInputStream inputZip = new ZipInputStream(input)) {
                    ZipEntry entry;
                    while ((entry = inputZip.getNextEntry()) != null) {
                        if (!names.add(entry.getName())) continue;
                        outputZip.putNextEntry(entry);
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = inputZip.read(buffer)) != -1) {
                            outputZip.write(buffer, 0, len);
                        }
                        inputZip.closeEntry();
                        outputZip.closeEntry();
                    }
                }
            }
        }
    }
}
