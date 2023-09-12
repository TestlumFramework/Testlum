package com.knubisoft.testlum.testing.framework.interpreter.lib.ui;

import com.knubisoft.testlum.testing.framework.ConditionUtil;
import com.knubisoft.testlum.testing.framework.configuration.global.GlobalTestConfigurationProviderImpl.ConfigProvider;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import com.knubisoft.testlum.testing.framework.util.JacksonMapperUtil;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.model.scenario.AbstractUiCommand;
import lombok.extern.slf4j.Slf4j;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
public abstract class AbstractUiExecutor<T extends AbstractUiCommand> {

    protected final ExecutorDependencies dependencies;

    protected AbstractUiExecutor(final ExecutorDependencies dependencies) {
        this.dependencies = dependencies;
    }

    public final void apply(final T o, final CommandResult result) {
        T t = injectObject(o, dependencies.getScenarioContext());
        result.setComment(t.getComment());
        LogUtil.logUICommand(result.getId(), t);
        if (ConditionUtil.isTrue(t.getCondition(), dependencies.getScenarioContext(), result)) {
            execute(t, result);
        }
    }

    protected abstract void execute(T o, CommandResult result);

    protected String inject(final String original) {
        return dependencies.getScenarioContext().inject(original);
    }

    protected String getContentIfFile(final String fileOrContent) {
        if (isNotBlank(fileOrContent)) {
            String content = FileSearcher.searchFileToString(fileOrContent, dependencies.getFile());
            return inject(content);
        }
        return fileOrContent;
    }

    protected void checkIfStopScenarioOnFailure(final Exception e) {
        if (ConfigProvider.provide().isStopScenarioOnFailure()) {
            throw new DefaultFrameworkException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T injectObject(final T t, final ScenarioContext scenarioContext) {
        String asJson = JacksonMapperUtil.writeValueToCopiedString(t);
        String injected = scenarioContext.inject(asJson);
        return JacksonMapperUtil.readCopiedValue(injected, (Class<T>) t.getClass());
    }
}
