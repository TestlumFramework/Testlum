package com.testlum.testing.framework.mcp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LayoutTest {

    @TempDir
    Path tmp;

    @Test
    void namesDifferingOnlyInCaseAreTheSameOnEverySystem() {
        assertTrue(Layout.sameName("Users.csv", "users.csv"));
        assertTrue(Layout.startsWithName("Group/Child", "group/"));
        assertFalse(Layout.sameName("users.csv", "user.csv"));
    }

    @Test
    void aPathThatOnlyDiffersInCaseFromAnExistingOneCountsAsExisting() throws IOException {
        Files.createDirectories(tmp.resolve("data"));
        Files.writeString(tmp.resolve("data/users.csv"), "x");
        assertTrue(Layout.existsAnyCase(tmp, tmp.resolve("data/users.csv")));
        assertTrue(Layout.existsAnyCase(tmp, tmp.resolve("DATA/Users.CSV")));
        assertFalse(Layout.existsAnyCase(tmp, tmp.resolve("data/other.csv")));
        assertFalse(Layout.existsAnyCase(tmp, tmp.resolve("nowhere/users.csv")));
    }
}
