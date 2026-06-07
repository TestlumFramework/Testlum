package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

@AllArgsConstructor
public class ContentResolver {

    private static final String JSON_EXTENSION = ".json";
    private final FileSearcher fileSearcher;
    private final ScenarioContext scenarioContext;
    private final File baseFile;

    public String getContentIfFile(final String fileOrContent) {
        if (StringUtils.isNotBlank(fileOrContent) && fileOrContent.endsWith(JSON_EXTENSION)) {
            String content = fileSearcher.searchFileToString(fileOrContent, baseFile);
            return scenarioContext.inject(content);
        }
        return fileOrContent;
    }

}
