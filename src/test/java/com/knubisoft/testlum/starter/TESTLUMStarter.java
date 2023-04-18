package com.knubisoft.testlum.starter;

import com.knubisoft.testlum.runner.Runner;
import com.knubisoft.testlum.runner.impl.InitialStructureGeneratorRunner;
import com.knubisoft.testlum.runner.impl.TestRunner;
import com.knubisoft.testlum.testing.framework.util.ArgumentsUtils;

public class TESTLUMStarter {

    public static void main(final String[] args) {
        ArgumentsUtils.validateInputArguments(args);
        Runner runner = args.length == 2 ? new TestRunner() : new InitialStructureGeneratorRunner();
        runner.run(args);
    }
}
