package com.knubisoft.testlum.testing.framework.locator;

import com.knubisoft.testlum.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.parser.XMLParsers;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import com.knubisoft.testlum.testing.model.pages.Component;
import com.knubisoft.testlum.testing.model.pages.Include;
import com.knubisoft.testlum.testing.model.pages.Locator;
import com.knubisoft.testlum.testing.model.pages.Page;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.UNABLE_PARSE_FILE_WITH_LOCATORS;
import static java.lang.String.format;

public class LocatorCollector {

    private static final PageValidator PAGE_VALIDATOR = new PageValidator();

    private final Map<String, File> pageFiles;
    private final Map<String, File> componentFiles;

    public LocatorCollector() {
        TestResourceSettings resourceSettings = TestResourceSettings.getInstance();
        this.pageFiles = FileSearcher.collectFilesFromFolder(resourceSettings.getPagesFolder());
        this.componentFiles = FileSearcher.collectFilesFromFolder(resourceSettings.getComponentsFolder());
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
        String prefix = each.getKey().getName().replace(TestResourceSettings.XML_SUFFIX, DelimiterConstant.EMPTY);
        return prefix + DelimiterConstant.DOT + locator.getLocator();
    }
}
