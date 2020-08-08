package de.skyrising.carpet.installer;

import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.ext.gfm.issues.GfmIssuesExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughSubscriptExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.gfm.users.GfmUsersExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import de.skyrising.carpet.installer.api.Api;
import de.skyrising.carpet.installer.utils.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class AbstractHandler implements Handler {
    private static final String CSS = "body {font-family: sans-serif}\n" +
            "a {color: inherit}\n" +
            "aside {text-align: right; font-weight: 300}";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    private static final Parser markdownParser;
    static {
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, Arrays.asList(
            GfmIssuesExtension.create(),
            GfmUsersExtension.create(),
            StrikethroughSubscriptExtension.create(),
            TaskListExtension.create()
        ));
        markdownParser = Parser.builder(options).build();
    }
    private static final HtmlRenderer htmlRenderer = HtmlRenderer.builder().build();

    public final String name;

    protected JPanel panel;
    protected BrowserComponent releaseInfoBrowser;
    protected Api.VersionInfo selectedVersion;
    protected Api.ReleaseInfo selectedRelease;
    protected JButton installButton;

    protected AbstractHandler(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    protected abstract boolean isValidVersion(Api.VersionInfo version);
    protected abstract boolean isValidRelease(Api.ReleaseInfo release);

    @Override
    public JPanel makePanel(InstallerGui gui) {
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JPanel selectionPanel = new JPanel();
        selectionPanel.setLayout(new GridLayout(2, 2));
        selectionPanel.getInsets().set(5, 5, 10, 5);
        releaseInfoBrowser = BrowserComponent.create();
        JComboBox<ListItemWrapper<Api.ReleaseInfo>> releasesBox = makeReleasesBox(r -> {
            releaseInfoBrowser.setSource(getHtml(r));
            selectedRelease = r;
        });
        JComboBox<ListItemWrapper<Api.VersionInfo>> versionsBox = makeVersionsBox(gui.api, v -> {
            updateReleases(gui.api, releasesBox, v);
            selectedVersion = v;
        });
        selectionPanel.add(new JLabel("Version:"));
        selectionPanel.add(versionsBox);
        selectionPanel.add(new JLabel("Release:"));
        selectionPanel.add(releasesBox);
        panel.add(selectionPanel, BorderLayout.PAGE_START);
        Component releaseInfoPane = releaseInfoBrowser.getComponent();
        Dimension descrSize = new Dimension(500, 300);
        releaseInfoPane.setMinimumSize(descrSize);
        releaseInfoPane.setPreferredSize(descrSize);
        panel.add(new JScrollPane(releaseInfoPane), BorderLayout.CENTER);
        JPanel bottom = new JPanel();
        bottom.setLayout(new BorderLayout());
        installButton = new JButton("Install");
        installButton.addActionListener(e -> {
            new Thread(() -> {
                onInstallButton(gui, selectedVersion, selectedRelease);
            }, "Install thread").start();
        });
        bottom.add(installButton, BorderLayout.LINE_END);
        panel.add(bottom, BorderLayout.PAGE_END);
        return panel;
    }

    protected abstract void onInstallButton(InstallerGui gui, Api.VersionInfo version, Api.ReleaseInfo release);

    @SuppressWarnings("unchecked")
    protected JComboBox<ListItemWrapper<Api.VersionInfo>> makeVersionsBox(Api api, Consumer<Api.VersionInfo> onSelect) {
        JComboBox<ListItemWrapper<Api.VersionInfo>> versionsBox = new JComboBox<>();
        versionsBox.setModel(new DefaultComboBoxModel<>(new ListItemWrapper[]{new ListItemWrapper<>(null,"Loading...")}));
        CompletableFuture.supplyAsync(api::getVersions).thenAccept(versions -> {
            List<Api.VersionInfo> validVersions = versions.stream().filter(this::isValidVersion).collect(Collectors.toList());
            ListItemWrapper<Api.VersionInfo>[] filtered = validVersions.stream()
                    .map(v -> new ListItemWrapper<>(v, v.version)).toArray(ListItemWrapper[]::new);
            versionsBox.setModel(new DefaultComboBoxModel<>(filtered));
            if (!validVersions.isEmpty()) {
                Api.VersionInfo defaultVersion = getDefaultVersion(validVersions);
                for (ListItemWrapper<Api.VersionInfo> item : filtered) {
                    if (item.item.equals(defaultVersion)) {
                        versionsBox.setSelectedItem(item);
                        break;
                    }
                }
            }
            onSelect.accept(getSelectedItem(versionsBox));
            versionsBox.addActionListener(e -> onSelect.accept(getSelectedItem(versionsBox)));
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
        return versionsBox;
    }

    protected abstract Api.VersionInfo getDefaultVersion(List<Api.VersionInfo> versions);

    @SuppressWarnings("unchecked")
    private static <T> T getSelectedItem(JComboBox<ListItemWrapper<T>> comboBox) {
        ListItemWrapper<T> wrapper = (ListItemWrapper<T>) comboBox.getSelectedItem();
        return wrapper == null ? null : wrapper.item;
    }

    protected JComboBox<ListItemWrapper<Api.ReleaseInfo>> makeReleasesBox(Consumer<Api.ReleaseInfo> onSelect) {
        JComboBox<ListItemWrapper<Api.ReleaseInfo>> releasesBox = new JComboBox<>();
        releasesBox.addActionListener(e -> onSelect.accept(getSelectedItem(releasesBox)));
        return releasesBox;
    }

    @SuppressWarnings("unchecked")
    protected void updateReleases(Api api, JComboBox<ListItemWrapper<Api.ReleaseInfo>> releasesBox, Api.VersionInfo version) {
        releasesBox.setModel(new DefaultComboBoxModel<>(new ListItemWrapper[]{new ListItemWrapper<>(null,"Loading...")}));
        CompletableFuture.supplyAsync(() -> api.getReleases(version.version)).thenAccept(releases -> {
            ListItemWrapper<Api.ReleaseInfo>[] filtered = releases.stream()
                    .filter(this::isValidRelease)
                    .map(v -> new ListItemWrapper<>(v, v.version)).toArray(ListItemWrapper[]::new);
            releasesBox.setModel(new DefaultComboBoxModel<>(filtered));
            releasesBox.setSelectedIndex(0);
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    private static String getHtml(Api.ReleaseInfo release) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html><head>");
        sb.append("<style type=\"text/css\">").append(CSS).append("</style>");
        sb.append("</head><body>");
        sb.append("<h1><a href=\"").append(release.infoUrl).append("\">").append(release.name).append("</a></h1>");
        sb.append("<aside>By ").append(release.author).append(" on ").append(DATE_FORMAT.format(release.releaseDate)).append("</aside>");
        String markdown = release.description;
        markdown = markdown.replaceAll("(^|\n)-(?=\\w)", "$1- ");
        Node description = markdownParser.parse(markdown);
        if (description.getFirstChild().isOrDescendantOfType(Paragraph.class) && !description.hasOrMoreChildren(2) && release.description.contains("\n")) {
            sb.append("<pre>").append(release.description).append("</pre>");
        } else {
            sb.append("<div>").append(htmlRenderer.render(description)).append("</div>");
        }
        sb.append("</body></html>");
        return sb.toString();
    }

    protected static Function<Boolean, CompletableFuture<Boolean>> downloadTask(ProgressDialog progress, Supplier<URL> url, File file) {
        return success -> {
            URL realURL = url.get();
            if (realURL == null) return CompletableFuture.completedFuture(false);
            try {
                FileOutputStream out = new FileOutputStream(file);
                return progress.runTask(new DownloadTask(realURL, out), success);
            } catch (IOException e) {
                return CompletableFuture.completedFuture(false);
            }
        };
    }

    protected static Function<Boolean, CompletableFuture<Boolean>> mergeTask(ProgressDialog progress, File outputJar, File ...inputs) {
        return success -> {
            FileInputStream[] inStreams = new FileInputStream[inputs.length];
            FileOutputStream out = null;
            try  {
                out = new FileOutputStream(outputJar);
                for (int i = 0; i < inputs.length; i++) {
                    inStreams[i] = new FileInputStream(inputs[i]);
                }
                FileOutputStream finalOut = out;
                return progress.runTask("Merging", () -> {
                    try {
                        new ZipMerger(finalOut, inStreams).merge();
                        return true;
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                }, success);
            } catch (IOException e) {
                try {
                    if (out != null) out.close();
                } catch (IOException e2) {
                    e.addSuppressed(e2);
                }
                for (InputStream in : inStreams) {
                    try {
                        if (in != null) in.close();
                    } catch (IOException e2) {
                        e.addSuppressed(e2);
                    }
                }
                return CompletableFuture.completedFuture(false);
            }
        };
    }
}
