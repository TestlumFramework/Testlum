package com.testlum.testing.framework.mcp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.testlum.testing.framework.mcp.exception.ToolException;
import com.testlum.testing.framework.mcp.constant.ToolMessage;
import tools.jackson.databind.JsonNode;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Works out which tests folder a tool call means. No dir means the mounted folder (/work in the container),
 * a relative dir is under it, and a host path under the folder the launcher mounted maps back to it. When the
 * server itself runs on Windows, a drive path is used as it is.
 */
@Component
final class WorkDirectoryResolver {

    static final String RULE = "Paths are inside the container: the mounted tests folder is /work. "
            + "Leave dir out to use it, or give a path under it such as /work/scenarios/x or a relative one.";

    private static final Pattern DRIVE = Pattern.compile("^[A-Za-z]:/.*");

    private final String root;
    private final String host;
    private final boolean windows;

    @Autowired
    WorkDirectoryResolver(@Value("${TESTLUM_DEFAULT_DIR:}") final String root,
                          @Value("${TESTLUM_HOST_WORKDIR:}") final String host) {
        this(root, host, File.separatorChar == '\\');
    }

    WorkDirectoryResolver(final String root, final String host, final boolean windows) {
        this.windows = windows;
        this.root = root == null || root.isBlank() ? defaultRoot() : root;
        this.host = host == null ? "" : slashes(host).replaceAll("/+$", "");
    }

    private static String defaultRoot() {
        return Files.isDirectory(Path.of("/work")) ? "/work" : System.getProperty("user.dir");
    }

    /** The folder the call is about, which must already exist. */
    Path existing(final JsonNode args) {
        Path dir = resolve(args);
        if (!Files.isDirectory(dir)) {
            throw new ToolException(ToolMessage.NOT_A_FOLDER, dir, RULE);
        }
        return dir;
    }

    Path resolve(final JsonNode args) {
        return Path.of(resolveText(args)).toAbsolutePath().normalize();
    }

    String resolveText(final JsonNode args) {
        String given = args.path("dir").asText("").trim();
        if (given.isEmpty()) {
            return root;
        }
        String p = slashes(given);
        if (underHost(p)) {
            return root + p.substring(host.length());
        }
        if (DRIVE.matcher(p).matches()) {
            return hostPath(p, given);
        }
        return p.startsWith("/") ? p : Path.of(root).resolve(p).normalize().toString();
    }

    private boolean underHost(final String p) {
        return !host.isEmpty() && p.toLowerCase(Locale.ROOT).startsWith(host.toLowerCase(Locale.ROOT))
                && (p.length() == host.length() || p.charAt(host.length()) == '/');
    }

    /** On Windows a drive path is a real path; in the container it names a place the server cannot see. */
    private String hostPath(final String p, final String given) {
        if (windows) {
            return p;
        }
        throw new ToolException(ToolMessage.HOST_PATH, given, RULE);
    }

    private static String slashes(final String s) {
        return s.replace('\\', '/');
    }
}
