package de.skyrising.carpet.installer.api;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Api {
    public static final String DEFAULT_URL = "https://carpet.skyrising.xyz";
    private static final Gson GSON = new Gson();

    private final String baseUrl;
    private final Map<String, VersionInfo> versions = new LinkedHashMap<>();
    private final Map<String, List<ReleaseInfo>> releases = new LinkedHashMap<>();

    public Api() {
        this(DEFAULT_URL);
    }

    public Api(String url) {
        this.baseUrl = url;
    }

    public JsonElement fetch(String path) {
        System.out.println("Fetching " + path);
        try {
            URL url = new URL(baseUrl + path);
            URLConnection conn = url.openConnection();
            conn.connect();
            return GSON.fromJson(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8), JsonElement.class);
        } catch (IOException e) {
            throw new ApiException(e);
        }
    }

    public static class VersionInfo {
        public final String version;
        public final String url;
        public final String gameUrl;
        public final boolean hasServer;
        public final boolean hasClient;

        private VersionInfo(Object o) {
            throw new IllegalStateException();
        }

        @Override
        public String toString() {
            return "Version " + version;
        }
    }

    private synchronized void fetchVersions() {
        if (!versions.isEmpty()) return;
        JsonElement data = fetch("/");
        VersionInfo[] versions = GSON.fromJson(data, VersionInfo[].class);
        for (VersionInfo info : versions) {
            this.versions.put(info.version, info);
        }
    }

    public Set<VersionInfo> getVersions() {
        fetchVersions();
        return Collections.unmodifiableSet(new LinkedHashSet<>(versions.values()));
    }

    public VersionInfo getVersion(String id) {
        fetchVersions();
        if (!versions.containsKey(id)) throw new ApiException(new IllegalArgumentException("Unknown version " + id));
        return versions.get(id);
    }

    public static class ReleaseInfo {
        public final String version;
        public final boolean stable;
        public final String name;
        public final String description;
        public final String author;
        public final Date releaseDate;
        public final String infoUrl;
        public final Map<String, String> downloads;

        private ReleaseInfo(Object o) {
            throw new IllegalStateException();
        }

        @Override
        public String toString() {
            return "Release " + version;
        }
    }

    private List<ReleaseInfo> fetchReleases(String version) {
        JsonElement data = fetch("/" + version + "/");
        return GSON.fromJson(data, new TypeToken<List<ReleaseInfo>>(){}.getType());
    }

    public synchronized List<ReleaseInfo> getReleases(String version) {
        return releases.computeIfAbsent(version, this::fetchReleases);
    }

    public static class ApiException extends RuntimeException {
        ApiException(Throwable cause) {
            super(cause);
        }

        ApiException(String message) {
            super(message);
        }

        ApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
