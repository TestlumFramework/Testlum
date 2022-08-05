package com.knubisoft.cott.testing.framework.locator;

import com.knubisoft.cott.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.parser.XMLParsers;
import com.knubisoft.cott.testing.model.pages.Component;
import com.knubisoft.cott.testing.model.pages.Include;
import com.knubisoft.cott.testing.model.pages.Locator;
import com.knubisoft.cott.testing.model.pages.Page;
import com.knubisoft.cott.testing.framework.util.FileSearcher;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.UNABLE_PARSE_FILE_WITH_LOCATORS;
import static java.lang.String.format;

public class LocatorCollector {

    private final File pagesFolder;
    private final File componentsFolder;

    public LocatorCollector() {
        TestResourceSettings resourceSettings = TestResourceSettings.getInstance();
        this.pagesFolder = resourceSettings.getPagesFolder();
        this.componentsFolder = resourceSettings.getComponentsFolder();
    }

    public Map<String, Locator> collect() {
        Map<File, Page> fileToPage = collectFileToPageMap();
        return transformToNameToLocatorMap(fileToPage);
    }

    private Map<File, Page> collectFileToPageMap() {
        Map<File, Page> fileToPage = new LinkedHashMap<>();
        File[] listFiles = Objects.requireNonNull(pagesFolder.listFiles());
        for (File each : listFiles) {
            fileToPage.put(each, parseLocatorOrThrow(each));
        }
        return fileToPage;
    }

    private Page parseLocatorOrThrow(final File each) {
        try {
            PageValidator pageValidator = new PageValidator();
            return XMLParsers.forPageLocator().process(each, pageValidator);
        } catch (Exception e) {
            throw new DefaultFrameworkException(
                    format(UNABLE_PARSE_FILE_WITH_LOCATORS, each.getName(), e.getMessage()), e);
        }
    }

    @NotNull
    private Map<String, Locator> transformToNameToLocatorMap(final Map<File, Page> fileToPage) {
        Map<String, Locator> result = new LinkedHashMap<>();

        for (Map.Entry<File, Page> each : fileToPage.entrySet()) {
            for (Locator locator : getLocators(each.getValue())) {
                result.put(getKeyName(each, locator), locator);
            }
        }
        return result;
    }

    private List<Locator> getLocators(final Page page) {
        List<Locator> result = page.getLocators().getLocator();

        for (Include include : page.getInclude()) {
            Component component = parseComponent(include);
            result.addAll(component.getLocators().getLocator());
        }
        return result;
    }

    private Component parseComponent(final Include include) {
        File file = FileSearcher.searchFileFromDir(componentsFolder, include.getComponent());
        return XMLParsers.forComponentLocator().process(file);
    }

    private String getKeyName(final Map.Entry<File, Page> each, final Locator locator) {
        String prefix = each.getKey().getName().replace(TestResourceSettings.XML_SUFFIX, DelimiterConstant.EMPTY)
                + DelimiterConstant.DOT;
        return prefix + locator.getLocatorId();
    }
}
