package com.knubisoft.testlum.testing.framework;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.exception.FileLinkingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FileSearcherTest {

    @TempDir
    File tempDir;

    private File dataFolder;
    private File envFolder;
    private File scenariosFolder;
    private FileSearcher fileSearcher;

    @BeforeEach
    void setUp() throws IOException {
        dataFolder = new File(tempDir, "data");
        dataFolder.mkdirs();
        envFolder = new File(tempDir, "config");
        envFolder.mkdirs();
        scenariosFolder = new File(tempDir, "scenarios");
        scenariosFolder.mkdirs();
        new File(tempDir, "global.xml").createNewFile();
        new File(tempDir, "locators/pages").mkdirs();
        new File(tempDir, "locators/component").mkdirs();

        TestResourceSettings.init("global.xml", tempDir.getAbsolutePath(), Optional.empty());
        fileSearcher = new FileSearcher(new TestResourceSettings());
    }

    @Nested
    class SearchFileFromDir {
        @Test
        void findsFileInDirectory() throws IOException {
            File expected = new File(scenariosFolder, "expected.json");
            Files.writeString(expected.toPath(), "{}", StandardCharsets.UTF_8);
            File found = fileSearcher.searchFileFromDir(scenariosFolder, "expected.json");
            assertEquals("expected.json", found.getName());
        }

        @Test
        void findsFileInParentDirectory() throws IOException {
            File subDir = new File(scenariosFolder, "sub");
            subDir.mkdirs();
            File expected = new File(scenariosFolder, "shared.json");
            Files.writeString(expected.toPath(), "{}", StandardCharsets.UTF_8);
            File found = fileSearcher.searchFileFromDir(subDir, "shared.json");
            assertEquals("shared.json", found.getName());
        }

        @Test
        void throwsWhenFileNotFound() {
            assertThrows(FileLinkingException.class,
                    () -> fileSearcher.searchFileFromDir(scenariosFolder, "nonexistent.json"));
        }

        @Test
        void stripsLeadingSlash() throws IOException {
            File expected = new File(scenariosFolder, "data.json");
            Files.writeString(expected.toPath(), "{}", StandardCharsets.UTF_8);
            File found = fileSearcher.searchFileFromDir(scenariosFolder, "/data.json");
            assertEquals("data.json", found.getName());
        }
    }

    @Nested
    class SearchFileToString {
        @Test
        void readsFileContent() throws IOException {
            File file = new File(scenariosFolder, "content.txt");
            Files.writeString(file.toPath(), "hello world", StandardCharsets.UTF_8);
            String content = fileSearcher.searchFileToString("content.txt", scenariosFolder);
            assertEquals("hello world", content);
        }

        @Test
        void throwsForNullDir() {
            assertThrows(NullPointerException.class,
                    () -> fileSearcher.searchFileToString("file.txt", null));
        }
    }

    @Nested
    class SearchFileFromDataFolder {
        @Test
        void findsFileInDataFolder() throws IOException {
            File dataFile = new File(dataFolder, "dataset.sql");
            Files.writeString(dataFile.toPath(), "SELECT 1", StandardCharsets.UTF_8);
            FileSearcher fs = new FileSearcher(new TestResourceSettings());
            File found = fs.searchFileFromDataFolder("dataset.sql");
            assertEquals("dataset.sql", found.getName());
        }

        @Test
        void throwsWhenFileNotInDataFolder() {
            assertThrows(FileLinkingException.class,
                    () -> fileSearcher.searchFileFromDataFolder("missing.sql"));
        }

        @Test
        void stripsLeadingSlashFromDataFolder() throws IOException {
            File dataFile = new File(dataFolder, "file.json");
            Files.writeString(dataFile.toPath(), "{}", StandardCharsets.UTF_8);
            FileSearcher fs = new FileSearcher(new TestResourceSettings());
            File found = fs.searchFileFromDataFolder("/file.json");
            assertEquals("file.json", found.getName());
        }
    }

    @Nested
    class SearchFileFromEnvFolder {
        @Test
        void findsFileInEnvSubfolder() throws IOException {
            File envSubFolder = new File(envFolder, "dev");
            envSubFolder.mkdirs();
            File envFile = new File(envSubFolder, "integration.xml");
            Files.writeString(envFile.toPath(), "<xml/>", StandardCharsets.UTF_8);
            FileSearcher fs = new FileSearcher(new TestResourceSettings());
            Optional<File> found = fs.searchFileFromEnvFolder("dev", "integration.xml");
            assertTrue(found.isPresent());
            assertEquals("integration.xml", found.get().getName());
        }

        @Test
        void returnsEmptyForMissingFile() throws IOException {
            File envSubFolder = new File(envFolder, "staging");
            envSubFolder.mkdirs();
            FileSearcher fs = new FileSearcher(new TestResourceSettings());
            Optional<File> found = fs.searchFileFromEnvFolder("staging", "nonexistent.xml");
            assertTrue(found.isEmpty());
        }

        @Test
        void throwsForMissingFolder() {
            assertThrows(FileLinkingException.class,
                    () -> fileSearcher.searchFileFromEnvFolder("nonexistent_env", "file.xml"));
        }
    }

    @Nested
    class CollectFilesFromFolder {
        @Test
        void collectsAllFiles() throws IOException {
            Files.writeString(new File(dataFolder, "a.txt").toPath(), "a");
            Files.writeString(new File(dataFolder, "b.txt").toPath(), "b");
            Map<String, File> files = fileSearcher.collectFilesFromFolder(dataFolder);
            assertEquals(2, files.size());
            assertTrue(files.containsKey("a.txt"));
        }

        @Test
        void collectsFilesRecursively() throws IOException {
            File sub = new File(dataFolder, "sub");
            sub.mkdirs();
            Files.writeString(new File(dataFolder, "root.txt").toPath(), "r");
            Files.writeString(new File(sub, "nested.txt").toPath(), "n");
            Map<String, File> files = fileSearcher.collectFilesFromFolder(dataFolder);
            assertEquals(2, files.size());
            assertTrue(files.containsKey("nested.txt"));
        }

        @Test
        void throwsOnDuplicateFilenames() throws IOException {
            File sub = new File(dataFolder, "sub");
            sub.mkdirs();
            Files.writeString(new File(dataFolder, "dup.txt").toPath(), "1");
            Files.writeString(new File(sub, "dup.txt").toPath(), "2");
            assertThrows(DefaultFrameworkException.class,
                    () -> fileSearcher.collectFilesFromFolder(dataFolder));
        }
    }

    @Nested
    class CollectFolderFilesFromFolder {
        @Test
        void collectsSubfolderFiles() throws IOException {
            File sub1 = new File(envFolder, "dev");
            sub1.mkdirs();
            Files.writeString(new File(sub1, "config.xml").toPath(), "<xml/>");
            Map<String, Map<String, File>> result =
                    fileSearcher.collectFolderFilesFromFolder(envFolder);
            assertNotNull(result.get("dev"));
            assertTrue(result.get("dev").containsKey("config.xml"));
        }
    }
}
