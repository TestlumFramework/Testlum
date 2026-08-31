package com.knubisoft.testlum.testing.framework.autohealing.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class HealedLocators {

    private List<String> xpaths;
    private List<String> cssSelectors;
    private String id;
    private String className;
    private String text;
}
