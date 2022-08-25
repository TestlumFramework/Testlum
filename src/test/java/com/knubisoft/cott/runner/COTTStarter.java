package com.knubisoft.cott.runner;

import com.knubisoft.cott.testing.framework.util.ArgumentsUtils;

public class COTTStarter {

    public static void main(final String[] args) {
        ArgumentsUtils.validateInputArguments(args);
        Runner runner = lookup(args);
        runner.run(args);
    }

    private static Runner lookup(final String[] args) {
        if (args.length == 2) {
            return new TestRunner();
        } else {
            return new GeneratorRunner();
        }
    }

    public interface Runner {
        void run(String[] args);
    }
}
