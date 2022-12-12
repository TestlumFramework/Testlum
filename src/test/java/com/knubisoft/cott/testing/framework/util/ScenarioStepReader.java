package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.model.scenario.AbstractCommand;
import com.knubisoft.cott.testing.model.scenario.Mobilebrowser;
import com.knubisoft.cott.testing.model.scenario.Native;
import com.knubisoft.cott.testing.model.scenario.Scenario;
import com.knubisoft.cott.testing.model.scenario.Web;
import lombok.Getter;

import java.util.List;

@Getter
public class ScenarioStepReader {
    private boolean web;
    private boolean mobilebrowser;
    private boolean natives;

    public ScenarioStepReader checkSteps(final Scenario scenario) {
        List<AbstractCommand> commands = scenario.getCommands();
        if (commands.stream().anyMatch(command -> command instanceof Web)) {
            this.web = true;
        }
        if (commands.stream().anyMatch(command -> command instanceof Mobilebrowser)) {
            this.mobilebrowser = true;
        }
        if (commands.stream().anyMatch(command -> command instanceof Native)) {
            this.natives = true;
        }
        return this;
    }
}
