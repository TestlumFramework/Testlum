package com.testlum.testing.framework.scenario;

import com.testlum.testing.framework.FileSearcher;
import com.testlum.testing.framework.TestResourceSettings;
import com.testlum.testing.framework.constant.DelimiterConstant;
import com.testlum.testing.framework.xml.XMLParsers;
import com.testlum.testing.model.scenario.AbstractCommand;
import com.testlum.testing.model.scenario.Auth;
import com.testlum.testing.model.scenario.Include;
import com.testlum.testing.model.scenario.Repeat;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class IncludeCycleFinder {

    private static final String CHAIN_SEPARATOR = " -> ";

    private final XMLParsers xmlParsers;
    private final FileSearcher fileSearcher;
    private final TestResourceSettings testResourceSettings;

    /**
     * Looks for a cycle reachable through the Include of {@code includedFile} written in {@code scenarioFile}.
     *
     * @return the cycle as a chain of scenario files, the first and the last one are the same
     */
    public Optional<List<File>> findCycle(final File scenarioFile, final File includedFile) {
        List<File> chain = new ArrayList<>(List.of(scenarioFile));
        return search(includedFile, chain, new HashSet<>());
    }

    public String format(final List<File> cycle) {
        return cycle.stream().map(this::toDisplayName).collect(Collectors.joining(CHAIN_SEPARATOR));
    }

    private Optional<List<File>> search(final File file,
                                        final List<File> chain,
                                        final Set<File> explored) {
        if (chain.contains(file)) {
            return Optional.of(closeCycle(chain, file));
        }
        if (!explored.add(file)) {
            return Optional.empty();
        }
        chain.add(file);
        try {
            return searchInIncludes(file, chain, explored);
        } finally {
            chain.remove(chain.size() - 1);
        }
    }

    private List<File> closeCycle(final List<File> chain, final File file) {
        List<File> cycle = new ArrayList<>(chain.subList(chain.indexOf(file), chain.size()));
        cycle.add(file);
        return cycle;
    }

    private Optional<List<File>> searchInIncludes(final File file,
                                                  final List<File> chain,
                                                  final Set<File> explored) {
        for (File next : readIncludedFiles(file)) {
            Optional<List<File>> cycle = search(next, chain, explored);
            if (cycle.isPresent()) {
                return cycle;
            }
        }
        return Optional.empty();
    }

    private List<File> readIncludedFiles(final File file) {
        try {
            List<File> included = new ArrayList<>();
            collectIncludes(xmlParsers.forScenario().process(file).getCommands(), included);
            return included;
        } catch (Exception e) {
            // an unreadable scenario is reported when it is validated itself
            return Collections.emptyList();
        }
    }

    private void collectIncludes(final List<AbstractCommand> commands, final List<File> included) {
        for (AbstractCommand command : commands) {
            if (command instanceof Include include) {
                resolveIncludedFile(include).ifPresent(included::add);
            } else if (command instanceof Repeat repeat) {
                collectIncludes(repeat.getCommands(), included);
            } else if (command instanceof Auth auth) {
                collectIncludes(auth.getCommands(), included);
            }
        }
    }

    private Optional<File> resolveIncludedFile(final Include include) {
        String path = include.getScenario();
        if (StringUtils.isBlank(path) || path.contains(DelimiterConstant.DOUBLE_OPEN_BRACE)) {
            return Optional.empty();
        }
        try {
            File folder = new File(testResourceSettings.getScenariosFolder(), path);
            return Optional.of(fileSearcher.searchFileFromDir(folder, TestResourceSettings.SCENARIO_FILENAME));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String toDisplayName(final File file) {
        Path scenariosRoot = testResourceSettings.getScenariosFolder().toPath();
        Path folder = file.toPath().getParent();
        return folder.startsWith(scenariosRoot)
                ? scenariosRoot.relativize(folder).toString().replace(File.separatorChar, '/')
                : file.getName();
    }
}
