package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.SubCommandRunnerImpl;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.model.scenario.WebView;
import io.appium.java_client.remote.SupportsContextSwitching;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Locale;
import java.util.Set;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.CANNOT_SWITCH_TO_WEBVIEW;

@ExecutorForClass(WebView.class)
public class WebViewExecutor extends AbstractUiExecutor<WebView> {

    @Autowired
    private SubCommandRunnerImpl subCommandRunner;

    public WebViewExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final WebView webView, final CommandResult result) {
        SupportsContextSwitching driver = (SupportsContextSwitching) dependencies.getDriver();
        Set<String> contextNames = driver.getContextHandles();
        String contextName = contextNames.stream()
                .filter(context -> context.toLowerCase(Locale.US).contains("web"))
                .findFirst()
                .orElseThrow(() -> new DefaultFrameworkException(CANNOT_SWITCH_TO_WEBVIEW));
        driver.context(contextName);

        LogUtil.startUiCommandsInWebView();
        this.subCommandRunner.runCommands(webView.getClickOrInputOrAssert(), result, dependencies);
        ((SupportsContextSwitching) dependencies.getDriver()).context("NATIVE_APP");
        LogUtil.endUiCommandsInWebView();
    }
}
