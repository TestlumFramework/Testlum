package com.testlum.testing.framework.mcp;

import com.testlum.testing.framework.mcp.exception.ToolException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileSwapTest {

    @TempDir
    Path dir;

    private Path first;
    private Path second;
    private Path third;

    private Map<Path, String> threeFiles() throws IOException {
        first = dir.resolve("first.txt");
        second = dir.resolve("second.txt");
        third = dir.resolve("third.txt");
        Files.writeString(first, "old first");
        Files.writeString(second, "old second");
        Map<Path, String> content = new LinkedHashMap<>();
        content.put(first, "new first");
        content.put(second, "new second");
        content.put(third, "new third");
        return content;
    }

    private static FileSwap.Mover failingOn(final String name) {
        return (from, to) -> {
            if (to.getFileName().toString().equals(name)) {
                throw new IOException("cannot write " + name);
            }
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
        };
    }

    private List<String> names() throws IOException {
        try (Stream<Path> files = Files.list(dir)) {
            return files.map(f -> f.getFileName().toString()).sorted().toList();
        }
    }

    @Test
    void replacesEveryFileWhenNothingFails() throws IOException {
        FileSwap.apply(threeFiles());
        assertEquals("new first", Files.readString(first));
        assertEquals("new second", Files.readString(second));
        assertEquals("new third", Files.readString(third));
        assertEquals(List.of("first.txt", "second.txt", "third.txt"), names());
    }

    @Test
    void aLaterMoveThatFailsPutsEveryOriginalBackAndRemovesTheNewFile() throws IOException {
        Map<Path, String> content = threeFiles();
        assertThrows(ToolException.class, () -> FileSwap.apply(content, failingOn("third.txt")));
        assertEquals("old first", Files.readString(first));
        assertEquals("old second", Files.readString(second));
        assertFalse(Files.exists(third));
        assertEquals(List.of("first.txt", "second.txt"), names());
    }

    @Test
    void aBackupIsKeptAndNamedWhenItsRestoreFails() throws IOException {
        Map<Path, String> content = threeFiles();
        // second.txt is moved in, then third.txt fails, then putting second.txt back fails as well
        int[] movesToSecond = {0};
        FileSwap.Mover mover = (from, to) -> {
            if (to.equals(second) && ++movesToSecond[0] == 2) {
                throw new IOException("cannot restore second");
            }
            failingOn("third.txt").move(from, to);
        };

        ToolException e = assertThrows(ToolException.class, () -> FileSwap.apply(content, mover));

        assertEquals("old first", Files.readString(first));
        assertTrue(e.getMessage().contains("originals kept at"), e.getMessage());
        assertTrue(e.getMessage().contains(second.toString()), e.getMessage());
        Path backup = names().stream().filter(n -> n.endsWith(".bak")).map(dir::resolve).findFirst().orElseThrow();
        assertEquals("old second", Files.readString(backup));
        assertTrue(e.getMessage().contains(backup.toString()), e.getMessage());
        Throwable cause = e.getCause();
        assertEquals("cannot write third.txt", cause.getMessage());
        assertEquals("cannot restore second", cause.getSuppressed()[0].getMessage());
    }
}
