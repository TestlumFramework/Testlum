package com.testlum.testing.framework.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebDownloadUtilTest {

    @TempDir
    Path tempDir;

    private final WebDownloadUtil webDownloadUtil = new WebDownloadUtil();

    @Test
    void removesDownloadedFilesButKeepsGeneratedArtifacts() throws IOException {
        create("scenario.xml");
        final Set<String> snapshot = webDownloadUtil.snapshotFileNames(tempDir);
        create("downloaded.pdf");
        create("patch_2.xml");
        create("action_1_actual.json");
        create("actual_image_compared_to_expected.png");
        create("included_login_patch_2.xml");
        create("included_login_action_1_actual.json");
        create("included_login_actual_image_compared_to_expected.png");

        webDownloadUtil.cleanupDownloadedFiles(tempDir, snapshot, false);

        assertTrue(exists("scenario.xml"));
        assertTrue(exists("patch_2.xml"));
        assertTrue(exists("action_1_actual.json"));
        assertTrue(exists("actual_image_compared_to_expected.png"));
        assertTrue(exists("included_login_patch_2.xml"));
        assertTrue(exists("included_login_action_1_actual.json"));
        assertTrue(exists("included_login_actual_image_compared_to_expected.png"));
        assertFalse(exists("downloaded.pdf"));
    }

    @Test
    void keepsEverythingWhenDownloadedFilesAreKept() throws IOException {
        create("scenario.xml");
        Set<String> snapshot = webDownloadUtil.snapshotFileNames(tempDir);
        create("downloaded.pdf");

        webDownloadUtil.cleanupDownloadedFiles(tempDir, snapshot, true);

        assertTrue(exists("downloaded.pdf"));
    }

    private void create(final String name) throws IOException {
        Files.writeString(tempDir.resolve(name), "content");
    }

    private boolean exists(final String name) {
        return Files.exists(tempDir.resolve(name));
    }
}
