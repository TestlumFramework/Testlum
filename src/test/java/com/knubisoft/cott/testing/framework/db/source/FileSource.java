package com.knubisoft.cott.testing.framework.db.source;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.util.LogMessage;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class FileSource implements Source {

    private final StringsSource stringSource;

    @SneakyThrows
    public FileSource(final File file) {
        if (!file.exists()) {
            throw new DefaultFrameworkException(LogMessage.FILE_NOT_EXIST, file);
        }
        String queries = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        this.stringSource = new StringsSource(queries);
    }

    @Override
    public List<String> getQueries() {
        return stringSource.getQueries();
    }
}
