package com.knubisoft.testlum.testing.framework.configuration;

import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import com.knubisoft.testlum.testing.model.global_config.Environment;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class EnvsValidator {
    public void validate(final String configFileName, final List<Environment> enabledEnvironments) {
        List<Optional<File>> configs = collectEnvsConfigsFilesFor(configFileName, enabledEnvironments);
        validateEnvsFileSetFor(configFileName, configs);
    }

    private List<Optional<File>> collectEnvsConfigsFilesFor(final String configFileName,
                                                            final List<Environment> enabledEnvironments) {
        return enabledEnvironments.stream()
                .map(env -> FileSearcher.searchFileFromEnvFolder(env.getFolder(), configFileName))
                .collect(Collectors.toList());
    }

    private void validateEnvsFileSetFor(final String configFileName, final List<Optional<File>> list) {
        long nullCount = list.stream()
                .filter(element -> !element.isPresent())
                .count();

        if (nullCount != 0
                && nullCount != list.size()) {
            throw new DefaultFrameworkException(
                    String.format(ExceptionMessage.ENVS_CONFIGS_FOLDER_FILES_STRUCTURE_INCOMPATIBLE, configFileName));
        }
    }
}
