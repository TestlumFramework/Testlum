package com.knubisoft.cott.testing.framework.locator;

import com.knubisoft.cott.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.parser.XMLParsers;
import com.knubisoft.cott.testing.model.pages.Component;
import com.knubisoft.cott.testing.model.pages.Include;
import com.knubisoft.cott.testing.model.pages.Locator;
import com.knubisoft.cott.testing.model.pages.Page;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.DUPLICATE_FILENAME_LOCATORS;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.UNABLE_PARSE_FILE_WITH_LOCATORS;
import static java.lang.String.format;

public class LocatorCollector {

    private static final PageValidator PAGE_VALIDATOR = new PageValidator();

    private final Map<String, File> pageFiles;

    private final Map<String, File> componentFiles;

    public LocatorCollector() {
        TestResourceSettings resourceSettings = TestResourceSettings.getInstance();
        this.pageFiles = collectFilesFromFolder(resourceSettings.getPagesFolder());
        this.componentFiles = collectFilesFromFolder(resourceSettings.getComponentsFolder());
    }

    public Map<String, Locator> collect() {
        Map<File, Page> fileToPage = collectFileToPageMap();
        return transformToNameToLocatorMap(fileToPage);
    }

    private Map<File, Page> collectFileToPageMap() {
        Map<File, Page> fileToPage = new LinkedHashMap<>();
        pageFiles.values().forEach(each -> fileToPage.put(each, parseLocatorOrThrow(each)));
        return fileToPage;
    }

    private Page parseLocatorOrThrow(final File each) {
        try {
            Page page = XMLParsers.forPageLocator().process(each);
            addIncludeLocators(page);
            PAGE_VALIDATOR.validate(page, each);
            return page;
        } catch (Exception e) {
            throw new DefaultFrameworkException(
                    format(UNABLE_PARSE_FILE_WITH_LOCATORS, each.getName(), e.getMessage()), e);
        }
    }

    private void addIncludeLocators(final Page page) {
        List<Locator> includes = page.getLocators().getLocator();

        for (Include include : page.getInclude()) {
            Component component = parseComponent(include);
            includes.addAll(component.getLocators().getLocator());
        }

    }

    private Component parseComponent(final Include include) {
        File file = componentFiles.get(include.getComponent());
        return XMLParsers.forComponentLocator().process(file);
    }

    private Map<String, Locator> transformToNameToLocatorMap(final Map<File, Page> fileToPage) {
        Map<String, Locator> result = new LinkedHashMap<>();

        for (Map.Entry<File, Page> each : fileToPage.entrySet()) {
            for (Locator locator : each.getValue().getLocators().getLocator()) {
                result.put(getKeyName(each, locator), locator);
            }
        }
        return result;
    }

    private String getKeyName(final Map.Entry<File, Page> each, final Locator locator) {
        String prefix = each.getKey().getName().replace(TestResourceSettings.XML_SUFFIX, DelimiterConstant.EMPTY)
                + DelimiterConstant.DOT;
        return prefix + locator.getLocatorId();
    }

    private Map<String, File> collectFilesFromFolder(final File filesource) {
        Map<String, File> files = new HashMap<>();
        FileUtils.listFiles(filesource, null, true)
                .forEach(file -> {
                    files.computeIfPresent(file.getName(), (key, value) -> {
                        throw new DefaultFrameworkException(
                                DUPLICATE_FILENAME_LOCATORS, filesource.getName(), file.getName());
                    });
                    files.put(file.getName(), file);
                });
        return Collections.unmodifiableMap(files);
    }
}
