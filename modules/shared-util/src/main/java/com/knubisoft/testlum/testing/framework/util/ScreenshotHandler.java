package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.report.CommandResult;

import java.io.File;

public interface ScreenshotHandler {

    void putScreenshotToResult(CommandResult result, File screenshot);
}
