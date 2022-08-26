package com.knubisoft.cott.starter;

import com.knubisoft.cott.runner.impl.GeneratorRunner;
import com.knubisoft.cott.runner.Runner;
import com.knubisoft.cott.runner.impl.TestRunner;
import com.knubisoft.cott.testing.framework.util.ArgumentsUtils;

public class COTTStarter {

    public static void main(final String[] args) {
        ArgumentsUtils.validateInputArguments(args);
        Runner runner = getAppropriateRunner(args);
        runner.run(args);
    }

    private static Runner getAppropriateRunner(final String[] args) {
        if (args.length == 2) {
            return new TestRunner();
        } else {
            return new GeneratorRunner();
        }
    }
}
