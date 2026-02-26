package com.knubisoft.testlum.testing.framework.interpreter.lib.ui;

import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.*;
import com.knubisoft.testlum.testing.model.scenario.AbstractUiCommand;
import com.knubisoft.testlum.testing.model.scenario.MobilebrowserRepeat;
import com.knubisoft.testlum.testing.model.scenario.NativeRepeat;
import com.knubisoft.testlum.testing.model.scenario.WebRepeat;
import lombok.RequiredArgsConstructor;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@RequiredArgsConstructor
public abstract class AbstractUiExecutor<T extends AbstractUiCommand> {

    protected final ExecutorDependencies dependencies;
    protected final UiUtil uiUtil;
    protected final ResultUtil resultUtil;
    protected final JavascriptUtil javascriptUtil;
    protected final ImageComparisonUtil imageComparisonUtil;
    protected final ConditionUtil conditionUtil;
    protected final ConfigUtil configUtil;
    protected final FileSearcher fileSearcher;
    protected final LogUtil logUtil;
    protected final InjectionUtil injectionUtil;

    public AbstractUiExecutor(final ExecutorDependencies dependencies) {
        this.dependencies = dependencies;
        this.uiUtil = dependencies.getContext().getBean(UiUtil.class);
        this.resultUtil = dependencies.getContext().getBean(ResultUtil.class);
        this.javascriptUtil = dependencies.getContext().getBean(JavascriptUtil.class);
        this.imageComparisonUtil = dependencies.getContext().getBean(ImageComparisonUtil.class);
        this.conditionUtil = dependencies.getContext().getBean(ConditionUtil.class);
        this.configUtil = dependencies.getContext().getBean(ConfigUtil.class);
        this.fileSearcher = dependencies.getContext().getBean(FileSearcher.class);
        this.logUtil = dependencies.getContext().getBean(LogUtil.class);
        this.injectionUtil = dependencies.getContext().getBean(InjectionUtil.class);
    }

    public final void apply(final T o, final CommandResult result) {
        T t = injectCommand(o);
        result.setComment(t.getComment());
        logUtil.logUICommand(result.getId(), t);
        if (conditionUtil.isTrue(t.getCondition(), dependencies.getScenarioContext(), result)) {
            execute(t, result);
        }
    }

    private T injectCommand(final T o) {
        if (!(o instanceof WebRepeat || o instanceof NativeRepeat || o instanceof MobilebrowserRepeat)) {
            return injectionUtil.injectObject(o, dependencies.getScenarioContext());
        }
        return o;
    }

    protected abstract void execute(T o, CommandResult result);

    protected String inject(final String original) {
        return dependencies.getScenarioContext().inject(original);
    }

    protected String getContentIfFile(final String fileOrContent) {
        if (isNotBlank(fileOrContent)) {
            String content = fileSearcher.searchFileToString(fileOrContent, dependencies.getFile());
            return inject(content);
        }
        return fileOrContent;
    }
}
