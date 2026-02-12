package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import com.knubisoft.testlum.testing.model.scenario.Mobilebrowser;
import com.knubisoft.testlum.testing.model.scenario.Native;
import com.knubisoft.testlum.testing.model.scenario.Scenario;
import com.knubisoft.testlum.testing.model.scenario.Web;
import lombok.Getter;

import java.util.List;

@Getter
public class ScenarioStepReader {

    private final boolean web;
    private final boolean mobileBrowser;
    private final boolean natives;

    public ScenarioStepReader(final Scenario scenario) {
        List<AbstractCommand> commands = scenario.getCommands();
        this.web = commands.stream().anyMatch(command -> command instanceof Web);
        this.mobileBrowser = commands.stream().anyMatch(command -> command instanceof Mobilebrowser);
        this.natives = commands.stream().anyMatch(command -> command instanceof Native);
    }
}
