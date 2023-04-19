package com.knubisoft.testlum.testing.framework.configuration;

import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider.getEnabledEnvironments;

@Slf4j
public class EnvsValidator {
    public void validate(final String configFileName) {
        List<Optional<File>> configs = collectEnvsConfigsFilesFor(configFileName);
        validateEnvsFileSetFor(configs);
    }

    private static List<Optional<File>> collectEnvsConfigsFilesFor(final String configFileName) {
        return getEnabledEnvironments().stream()
                .map(env -> FileSearcher.searchFileFromEnvFolder(env.getFolder(), configFileName))
                .collect(Collectors.toList());
    }

    private static <T> void validateEnvsFileSetFor(final List<Optional<T>> list) {
        long nullCount = list.stream()
                .filter(element -> !element.isPresent())
                .count();

        if (nullCount >= 1
                && nullCount != list.size()) {
            throw new DefaultFrameworkException(ExceptionMessage.ENVS_CONFIGS_FOLDER_FILES_STRUCTURE_INCOMPATIBLE);
        }
    }
}
