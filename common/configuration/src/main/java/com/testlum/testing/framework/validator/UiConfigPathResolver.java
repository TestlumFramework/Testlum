package com.testlum.testing.framework.validator;

import com.testlum.testing.framework.FileSearcher;
import com.testlum.testing.framework.TestResourceSettings;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@RequiredArgsConstructor
public class UiConfigPathResolver {

    private final TestResourceSettings settings;
    private final FileSearcher fileSearcher;

    public String resolve(final String envName) {
        return fileSearcher.searchFileFromEnvFolder(envName, TestResourceSettings.UI_CONFIG_FILENAME)
                .map(File::getPath).orElse(settings.getEnvConfigFolder().getPath())
                .replace(settings.getTestResourcesFolder().getPath(), StringUtils.EMPTY);
    }
}
