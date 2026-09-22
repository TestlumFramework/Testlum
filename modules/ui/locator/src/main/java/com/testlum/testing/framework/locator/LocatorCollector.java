package com.testlum.testing.framework.locator;

import com.testlum.testing.framework.FileSearcher;
import com.testlum.testing.framework.TestResourceSettings;
import com.testlum.testing.framework.constant.DelimiterConstant;
import com.testlum.testing.framework.constant.ExceptionMessage;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.xml.XMLParsers;
import com.testlum.testing.model.pages.Component;
import com.testlum.testing.model.pages.Include;
import com.testlum.testing.model.pages.Locator;
import com.testlum.testing.model.pages.Page;

import java.io.File;
import java.util.*;

@org.springframework.stereotype.Component
public class LocatorCollector {

    private final PageValidator pageValidator;
    private final XMLParsers xmlParsers;
    private final List<PageDefinition> pageDefinitions = new ArrayList<>();

    private final Map<String, File> pageFiles;
    private final Map<String, File> componentFiles;
    private final Map<String, LocatorData> locatorMap;

    public LocatorCollector(final XMLParsers xmlParsers,
                            final PageValidator pageValidator,
                            final TestResourceSettings testResourceSettings,
                            final FileSearcher fileSearcher) {
        this.pageValidator = pageValidator;
        this.xmlParsers = xmlParsers;
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
                .filter(file -> !file.getName().startsWith(TestResourceSettings.PATCH_FILE_PREFIX))
                .forEach(each -> fileToPage.put(each, parseLocatorOrThrow(xmlParsers, each)));
        return fileToPage;
    }

    private Page parseLocatorOrThrow(final XMLParsers xmlParsers, final File each) {
        try {
            Page page = xmlParsers.forPageLocator().process(each);
            List<Locator> ownLocators = List.copyOf(page.getLocators().getLocator());
            addIncludeLocators(page, xmlParsers);
            pageValidator.validate(page, each);
            pageDefinitions.add(new PageDefinition(pageName(each), page, ownLocators));
            return page;
        } catch (Exception e) {
            throw new DefaultFrameworkException(
                    String.format(ExceptionMessage.UNABLE_PARSE_FILE_WITH_LOCATORS, each.getName(), e.getMessage()), e);
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
        return pageName(each.getKey()) + DelimiterConstant.DOT + locator.getLocatorId();
    }

    private String pageName(final File pageFile) {
        return pageFile.getName().replace(TestResourceSettings.XML_SUFFIX, DelimiterConstant.EMPTY);
    }

    /**
     * @return every parsed page, with its own locators kept apart from those of its included components
     */
    public List<PageDefinition> getPageDefinitions() {
        return Collections.unmodifiableList(pageDefinitions);
    }

    /**
     * Parses every component file afresh, so callers never share locator instances with the pages.
     *
     * @return component name (as referenced by page includes) to parsed component
     */
    public Map<String, Component> getComponents() {
        Map<String, Component> components = new LinkedHashMap<>();
        componentFiles.forEach((name, file) -> components.put(name, xmlParsers.forComponentLocator().process(file)));
        return components;
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
            return new DefaultFrameworkException(ExceptionMessage.INCORRECT_NAMING_FOR_LOCATOR_ID, name);
        }
        return new DefaultFrameworkException(ExceptionMessage.UNABLE_TO_FIND_LOCATOR_BY_PATH, name);
    }
}
