package com.knubisoft.testlum.testing.framework.autohealing.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.json.JSONPropertyIgnore;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HealedLocators {

    private List<String> xpaths;
    private List<String> cssSelectors;
    private String id;
    private String className;
    private String text;

}
