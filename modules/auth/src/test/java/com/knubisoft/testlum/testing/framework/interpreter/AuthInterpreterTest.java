package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.auth.AuthFactory;
import com.knubisoft.testlum.testing.framework.auth.AuthStrategy;
import com.knubisoft.testlum.testing.framework.configuration.ConfigProvider;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import com.knubisoft.testlum.testing.framework.util.ConditionProvider;
import com.knubisoft.testlum.testing.framework.util.JacksonService;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.knubisoft.testlum.testing.model.scenario.Auth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthInterpreterTest {

    private AuthInterpreter interpreter;
    private AuthFactory authFactory;
    private AuthStrategy authStrategy;
    private JacksonService jacksonService;
    private ConditionProvider conditionProvider;

    @BeforeEach
    void setUp() {
        authFactory = mock(AuthFactory.class);
        authStrategy = mock(AuthStrategy.class);
        jacksonService = mock(JacksonService.class);
        conditionProvider = mock(ConditionProvider.class);
        GlobalTestConfiguration globalConfig = mock(GlobalTestConfiguration.class);
        when(globalConfig.isStopScenarioOnFailure()).thenReturn(false);

        final ApplicationContext context = mock(ApplicationContext.class);
        when(context.getBean(AuthFactory.class)).thenReturn(authFactory);
        when(context.getBean(ConfigProvider.class)).thenReturn(mock(ConfigProvider.class));
        when(context.getBean(ConditionProvider.class)).thenReturn(conditionProvider);
        when(context.getBean(FileSearcher.class)).thenReturn(mock(FileSearcher.class));
        when(context.getBean(JacksonService.class)).thenReturn(jacksonService);
        when(context.getBean(StringPrettifier.class)).thenReturn(mock(StringPrettifier.class));
        when(context.getBean(GlobalTestConfiguration.class)).thenReturn(globalConfig);

        ScenarioContext scenarioContext = new ScenarioContext(new HashMap<>());
        InterpreterDependencies dependencies = InterpreterDependencies.builder()
                .context(context)
                .file(new File("test.xml"))
                .scenarioContext(scenarioContext)
                .position(new AtomicInteger(1))
                .environment("test")
                .build();

        interpreter = new AuthInterpreter(dependencies);
    }

    @Test
    void applySetsMetaDataAndDelegatesToAuthStrategy() {
        Auth auth = new Auth();
        auth.setApiAlias("api1");
        auth.setLoginEndpoint("/login");
        auth.setCredentials("creds.json");

        when(jacksonService.writeValueToCopiedString(any())).thenReturn(
                "{\"apiAlias\":\"api1\",\"loginEndpoint\":\"/login\",\"credentials\":\"creds.json\"}");
        when(jacksonService.readCopiedValue(anyString(), eq(Auth.class))).thenReturn(auth);
        when(authFactory.create(any(), eq("api1"))).thenReturn(authStrategy);
        when(conditionProvider.isTrue(any(), any(), any())).thenReturn(true);

        CommandResult result = new CommandResult();
        result.setId(1);
        interpreter.apply(auth, result);

        verify(authStrategy).authenticate(auth, result);
        assertEquals("api1", result.getMetadata().get("API alias"));
        assertEquals("/login", result.getMetadata().get("Endpoint"));
        assertEquals("creds.json", result.getMetadata().get("Credentials file"));
    }

    @Test
    void applySetsDefaultAliasWhenNull() {
        Auth auth = new Auth();
        auth.setApiAlias(null);
        auth.setLoginEndpoint("/login");
        auth.setCredentials("creds.json");

        Auth injectedAuth = new Auth();
        injectedAuth.setApiAlias(null);
        injectedAuth.setLoginEndpoint("/login");
        injectedAuth.setCredentials("creds.json");

        when(jacksonService.writeValueToCopiedString(any())).thenReturn(
                "{\"loginEndpoint\":\"/login\",\"credentials\":\"creds.json\"}");
        when(jacksonService.readCopiedValue(anyString(), eq(Auth.class))).thenReturn(injectedAuth);
        when(authFactory.create(any(), eq("DEFAULT"))).thenReturn(authStrategy);
        when(conditionProvider.isTrue(any(), any(), any())).thenReturn(true);

        CommandResult result = new CommandResult();
        result.setId(1);
        interpreter.apply(auth, result);

        verify(authFactory).create(any(), eq("DEFAULT"));
    }
}
