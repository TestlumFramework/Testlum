package com.knubisoft.e2e.testing.framework.interpreter;

import com.knubisoft.e2e.testing.framework.interpreter.lib.AbstractSeleniumInterpreter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.model.scenario.NavigateTo;
import lombok.extern.slf4j.Slf4j;

import static com.knubisoft.e2e.testing.framework.util.LogMessage.BY_URL_LOG;

@Slf4j
@InterpreterForClass(NavigateTo.class)
public class NavigateToInterpreter extends AbstractSeleniumInterpreter<NavigateTo> {

    public NavigateToInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final NavigateTo o, final CommandResult result) {
        String url = inject(dependencies.getGlobalTestConfiguration().getUi().getBaseUrl() + o.getPath());
        log.info(BY_URL_LOG, url);
        dependencies.getWebDriver().navigate().to(url);
    }
}
