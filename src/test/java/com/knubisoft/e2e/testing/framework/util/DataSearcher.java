package com.knubisoft.e2e.testing.framework.util;

import com.knubisoft.e2e.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.e2e.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.e2e.testing.framework.exception.FileLinkingException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@UtilityClass
public class DataSearcher {
    public File fileByNameAndExtension(final String fileName) {
        return getDataFileset().stream().filter(f -> f.getName().equals(fileName)).findFirst()
                .orElseThrow(() -> new FileLinkingException(TestResourceSettings.getInstance().getDataFolder(),
                        TestResourceSettings.getInstance().getTestResourcesFolder(), fileName));

    }

    public Set<File> getDataFileset() {
        Set<File> files = new HashSet<>(FileUtils.listFiles(TestResourceSettings.getInstance().getDataFolder(),
                null, true));
        if (files.stream().map(File::getName).allMatch(new HashSet<String>()::add)){
            throw new DefaultFrameworkException("There are the same filename in data subdirectories");
        }
        return files;
    }
}
