package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.framework.variations.GlobalVariationsProvider;
import com.knubisoft.testlum.testing.framework.xml.XMLParsers;
import com.knubisoft.testlum.testing.model.scenario.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioStepReader {

    private final XMLParsers xmlParsers;
    private final FileSearcher fileSearcher;
    private final TestResourceSettings testResourceSettings;
    private final GlobalVariationsProvider globalVariationsProvider;

    public Result scan(final Scenario scenario,
                       final File scenarioFile,
                       final Map<File, Scenario> parsedScenarios) {
        ScanContext context = new ScanContext(scenarioFile, parsedScenarios);
        context.visited.add(scenarioFile);
        collectVariations(context, scenarioVariations(scenario));
        walk(scenario.getCommands(), context);
        return new Result(context.isWebPresent, context.isMobileBrowserPresent, context.isNativePresent);
    }

    private String scenarioVariations(final Scenario scenario) {
        Settings settings = scenario.getSettings();
        return Objects.isNull(settings) ? null : settings.getVariations();
    }

    private void walk(final List<AbstractCommand> commands, final ScanContext context) {
        for (AbstractCommand command : commands) {
            if (context.isAllTypesAlreadyFound()) {
                return;
            }
            walkCommand(command, context);
        }
    }

    private void walkCommand(final AbstractCommand command, final ScanContext context) {
        if (command instanceof Web) {
            context.isWebPresent = true;
        } else if (command instanceof Mobilebrowser) {
            context.isMobileBrowserPresent = true;
        } else if (command instanceof Native) {
            context.isNativePresent = true;
        } else if (command instanceof Auth auth) {
            walk(auth.getCommands(), context);
        } else if (command instanceof Repeat repeat) {
            walkRepeat(repeat, context);
        } else if (command instanceof Include include) {
            walkInclude(include, context);
        }
    }

    private void walkRepeat(final Repeat repeat, final ScanContext context) {
        collectVariations(context, repeat.getVariations());
        walk(repeat.getCommands(), context);
    }

    private void walkInclude(final Include include, final ScanContext context) {
        for (File includedFile : resolveIncludedFiles(include, context)) {
            if (context.visited.add(includedFile)) {
                loadScenario(includedFile, context)
                        .ifPresent(included -> walk(included.getCommands(), context));
            }
        }
    }

    private List<File> resolveIncludedFiles(final Include include, final ScanContext context) {
        Set<String> paths = resolveIncludePaths(include.getScenario(), context);
        if (paths.isEmpty()) {
            log.warn(LogMessage.UNRESOLVED_INCLUDE_PATH_LOG, include.getScenario(), context.rootFile);
            return Collections.emptyList();
        }
        List<File> files = new ArrayList<>();
        for (String path : paths) {
            searchIncludedFile(path, context).ifPresent(files::add);
        }
        return files;
    }

    private Set<String> resolveIncludePaths(final String rawPath, final ScanContext context) {
        if (StringUtils.isBlank(rawPath)) {
            return Collections.emptySet();
        }
        String path = rawPath.trim();
        if (!path.contains(DelimiterConstant.DOUBLE_OPEN_BRACE)) {
            return Collections.singleton(path);
        }
        Set<String> resolved = new LinkedHashSet<>();
        for (Map<String, String> variation : context.variations) {
            resolveByVariation(path, variation).ifPresent(resolved::add);
        }
        return resolved;
    }

    private Optional<String> resolveByVariation(final String path, final Map<String, String> variation) {
        try {
            String resolved = globalVariationsProvider.getValue(path, variation);
            return resolved.contains(DelimiterConstant.DOUBLE_OPEN_BRACE)
                    ? Optional.empty()
                    : Optional.of(resolved);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<File> searchIncludedFile(final String path, final ScanContext context) {
        try {
            File includedScenarioFolder = new File(testResourceSettings.getScenariosFolder(), path);
            return Optional.of(fileSearcher.searchFileFromDir(includedScenarioFolder,
                    TestResourceSettings.SCENARIO_FILENAME));
        } catch (Exception e) {
            log.warn(LogMessage.UNREADABLE_INCLUDE_LOG, path, context.rootFile, e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<Scenario> loadScenario(final File includedFile, final ScanContext context) {
        Scenario cached = context.parsedScenarios.get(includedFile);
        if (Objects.nonNull(cached)) {
            return Optional.of(cached);
        }
        try {
            return Optional.of(xmlParsers.forScenario().process(includedFile));
        } catch (Exception e) {
            log.warn(LogMessage.UNREADABLE_INCLUDE_LOG, includedFile, context.rootFile, e.getMessage());
            return Optional.empty();
        }
    }

    private void collectVariations(final ScanContext context, final String variationsFile) {
        if (StringUtils.isBlank(variationsFile) || !context.variationFiles.add(variationsFile)) {
            return;
        }
        try {
            context.variations.addAll(globalVariationsProvider.getVariations(variationsFile));
        } catch (Exception e) {
            log.warn(LogMessage.UNREADABLE_VARIATIONS_LOG, variationsFile, context.rootFile, e.getMessage());
        }
    }

    public record Result(boolean isWebPresent, boolean isMobileBrowserPresent, boolean isNativePresent) {
    }

    @RequiredArgsConstructor
    private static final class ScanContext {
        private final File rootFile;
        private final Map<File, Scenario> parsedScenarios;
        private final Set<File> visited = new HashSet<>();
        private final Set<String> variationFiles = new HashSet<>();
        private final List<Map<String, String>> variations = new ArrayList<>();
        private boolean isWebPresent;
        private boolean isMobileBrowserPresent;
        private boolean isNativePresent;

        private boolean isAllTypesAlreadyFound() {
            return this.isWebPresent && this.isMobileBrowserPresent && this.isNativePresent;
        }
    }
}
