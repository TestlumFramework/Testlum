package com.testlum.testing.framework.interpreter.lib.ui.executor;

import com.testlum.testing.framework.constant.ExceptionMessage;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.interpreter.lib.SubCommandRunnerImpl;
import com.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.model.scenario.WebView;
import io.appium.java_client.remote.SupportsContextSwitching;

import java.util.Locale;
import java.util.Set;

@ExecutorForClass(WebView.class)
public class WebViewExecutor extends AbstractUiExecutor<WebView> {

    private final SubCommandRunnerImpl subCommandRunner;

    public WebViewExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        this.subCommandRunner = dependencies.getContext().getBean(SubCommandRunnerImpl.class);
    }

    @Override
    public void execute(final WebView webView, final CommandResult result) {
        SupportsContextSwitching driver = (SupportsContextSwitching) dependencies.getDriver();
        Set<String> contextNames = driver.getContextHandles();
        String contextName = contextNames.stream()
                .filter(context -> context.toLowerCase(Locale.US).contains("web"))
                .findFirst()
                .orElseThrow(() -> new DefaultFrameworkException(ExceptionMessage.CANNOT_SWITCH_TO_WEBVIEW));
        driver.context(contextName);

        uiLogUtil.startUiCommandsInWebView();
        this.subCommandRunner.runCommands(webView.getClickOrInputOrAssert(), result, dependencies);
        ((SupportsContextSwitching) dependencies.getDriver()).context("NATIVE_APP");
        uiLogUtil.endUiCommandsInWebView();
    }
}
