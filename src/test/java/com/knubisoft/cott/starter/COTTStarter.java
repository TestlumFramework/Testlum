package com.knubisoft.cott.starter;

import com.knubisoft.cott.runner.impl.InitialStructureGeneratorRunner;
import com.knubisoft.cott.runner.Runner;
import com.knubisoft.cott.runner.impl.TestRunner;
import com.knubisoft.cott.testing.framework.util.ArgumentsUtils;

public class COTTStarter {

    public static void main(final String[] args) {
        ArgumentsUtils.validateInputArguments(args);
        Runner runner = args.length == 2 ? new TestRunner() : new InitialStructureGeneratorRunner();
        runner.run(args);
    }
}
