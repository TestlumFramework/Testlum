package com.knubisoft.testlum.testing.framework.autohealing.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "healedLocator")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class HealedLocators {

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "xpath")
    private List<String> xpaths;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "cssSelector")
    private List<String> cssSelectors;
    private String id;
    private String className;
    private String text;
}
