package com.knubisoft.testlum.starter;

import com.knubisoft.testlum.runner.Runner;
import com.knubisoft.testlum.runner.impl.TestRunner;

public class TESTLUMStarter {

    public static void main(final String[] args) {
        Runner runner = new TestRunner();
        runner.run(args);
    }
}
