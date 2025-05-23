package com.knubisoft.testlum.testing.framework.interpreter.lib.ui;

import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ConditionProviderImpl.ConditionUtil;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import com.knubisoft.testlum.testing.framework.util.InjectionUtil;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.model.scenario.AbstractUiCommand;
import com.knubisoft.testlum.testing.model.scenario.MobilebrowserRepeat;
import com.knubisoft.testlum.testing.model.scenario.NativeRepeat;
import com.knubisoft.testlum.testing.model.scenario.WebRepeat;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public abstract class AbstractUiExecutor<T extends AbstractUiCommand> {

    protected final ExecutorDependencies dependencies;

    protected AbstractUiExecutor(final ExecutorDependencies dependencies) {
        this.dependencies = dependencies;
    }

    public final void apply(final T o, final CommandResult result) {
        T t = injectCommand(o);
        result.setComment(t.getComment());
        LogUtil.logUICommand(result.getId(), t);
        if (ConditionUtil.isTrue(t.getCondition(), dependencies.getScenarioContext(), result)) {
            execute(t, result);
        }
    }

    private T injectCommand(final T o) {
        if (!(o instanceof WebRepeat || o instanceof NativeRepeat || o instanceof MobilebrowserRepeat)) {
            return InjectionUtil.injectObject(o, dependencies.getScenarioContext());
        }
        return o;
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
}
