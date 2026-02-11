package com.knubisoft.testlum.testing.framework.locator;

import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.xml.XMLParsers;
import com.knubisoft.testlum.testing.model.pages.Component;
import com.knubisoft.testlum.testing.model.pages.Include;
import com.knubisoft.testlum.testing.model.pages.Locator;
import com.knubisoft.testlum.testing.model.pages.Page;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.*;
import static java.lang.String.format;

@org.springframework.stereotype.Component
public class LocatorCollector {

    private final PageValidator pageValidator;

    private final Map<String, File> pageFiles;
    private final Map<String, File> componentFiles;
    private final Map<String, LocatorData> locatorMap;

    public LocatorCollector(final XMLParsers xmlParsers,
                            final PageValidator pageValidator,
                            final TestResourceSettings testResourceSettings,
                            final FileSearcher fileSearcher) {
        this.pageValidator = pageValidator;
        this.pageFiles = fileSearcher.collectFilesFromFolder(testResourceSettings.getPagesFolder());
        this.componentFiles = fileSearcher.collectFilesFromFolder(testResourceSettings.getComponentsFolder());
        this.locatorMap = collect(xmlParsers);
    }

    private Map<String, LocatorData> collect(final XMLParsers xmlParsers) {
        Map<File, Page> fileToPage = collectFileToPageMap(xmlParsers);
        return transformToNameToLocatorMap(fileToPage);
    }


    private Map<File, Page> collectFileToPageMap(final XMLParsers xmlParsers) {
        Map<File, Page> fileToPage = new LinkedHashMap<>();
        pageFiles.values().stream()
                .filter(file -> !file.getName().startsWith("patch_"))
                .forEach(each -> fileToPage.put(each, parseLocatorOrThrow(xmlParsers, each)));
        return fileToPage;
    }

    private Page parseLocatorOrThrow(final XMLParsers xmlParsers, final File each) {
        try {
            Page page = xmlParsers.forPageLocator().process(each);
            addIncludeLocators(page, xmlParsers);
            pageValidator.validate(page, each);
            return page;
        } catch (Exception e) {
            throw new DefaultFrameworkException(
                    format(UNABLE_PARSE_FILE_WITH_LOCATORS, each.getName(), e.getMessage()), e);
        }
    }

    private void addIncludeLocators(final Page page, final XMLParsers xmlParsers) {
        List<Locator> includes = page.getLocators().getLocator();

        for (Include include : page.getInclude()) {
            Component component = parseComponent(include, xmlParsers);
            includes.addAll(component.getLocators().getLocator());
        }
    }

    private Component parseComponent(final Include include, final XMLParsers xmlParsers) {
        File file = componentFiles.get(include.getComponent());
        return xmlParsers.forComponentLocator().process(file);
    }

    private Map<String, LocatorData> transformToNameToLocatorMap(final Map<File, Page> fileToPage) {
        Map<String, LocatorData> result = new LinkedHashMap<>();

        for (Map.Entry<File, Page> each : fileToPage.entrySet()) {
            for (Locator locator : each.getValue().getLocators().getLocator()) {
                result.put(getKeyName(each, locator), new LocatorData(each.getKey(), locator));
            }
        }
        return result;
    }

    private String getKeyName(final Map.Entry<File, Page> each, final Locator locator) {
        String prefix = each.getKey().getName().replace(TestResourceSettings.XML_SUFFIX, DelimiterConstant.EMPTY);
        return prefix + DelimiterConstant.DOT + locator.getLocatorId();
    }

    public LocatorData getLocator(final String name) {
        LocatorData locatorData = locatorMap.get(name);
        if (locatorData == null) {
            throw defaultFrameworkException(name);
        }
        return locatorData;
    }

    private DefaultFrameworkException defaultFrameworkException(final String name) {
        if (name.split(DelimiterConstant.DOT_REGEX).length != 2) {
            return new DefaultFrameworkException(INCORRECT_NAMING_FOR_LOCATOR_ID, name);
        }
        return new DefaultFrameworkException(UNABLE_TO_FIND_LOCATOR_BY_PATH, name);
    }
}
