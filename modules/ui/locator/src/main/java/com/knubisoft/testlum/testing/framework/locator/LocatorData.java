package com.knubisoft.testlum.testing.framework.locator;

import com.knubisoft.testlum.testing.model.pages.Locator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.File;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LocatorData {

    private File file;
    private Locator locator;

}
