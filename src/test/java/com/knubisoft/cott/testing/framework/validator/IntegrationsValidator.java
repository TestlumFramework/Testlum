package com.knubisoft.cott.testing.framework.validator;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.model.global_config.Environment;
import com.knubisoft.cott.testing.model.global_config.Integration;
import com.knubisoft.cott.testing.model.global_config.Integrations;
import org.junit.platform.commons.util.ReflectionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.SAME_INTEGRATION_ALIASES;

public class IntegrationsValidator implements XMLValidator<Integrations> {
    private final Environment env;

    private final List<List<? extends Integration>> integrationLists = new ArrayList<>();

    public IntegrationsValidator(final Environment env) {
        this.env = env;
    }

    @Override
    public void validate(final Integrations integrations, final File xmlFile) {
        getAllExistingIntegrations(integrations);
        checkIntegrationsAliases();
    }

    private void getAllExistingIntegrations(final Integrations integrations) {
        Arrays.stream(integrations.getClass().getDeclaredMethods())
                .filter(method -> Objects.nonNull(method) && method.getName().startsWith("get"))
                .map(method -> ReflectionUtils.invokeMethod(method, integrations))
                .filter(Objects::nonNull)
                .forEach(integrationObject -> Arrays.stream(integrationObject.getClass().getDeclaredMethods())
                        .filter(method -> Objects.nonNull(method) && method.getReturnType().equals(List.class))
                        .map(method -> ReflectionUtils.invokeMethod(method, integrationObject))
                        .forEach(integrationList ->
                                integrationLists.add((List<? extends Integration>) integrationList)));
    }

    private void checkIntegrationsAliases() {
        integrationLists.forEach(integrationList -> integrationList.forEach(integration -> {
                    if (integrationList.stream().map(Integration::getAlias)
                            .filter(alias -> alias.equalsIgnoreCase(integration.getAlias())).count() > 1) {
                        throw new DefaultFrameworkException(SAME_INTEGRATION_ALIASES,
                                integration.getClass().getSimpleName(), integration.getAlias(), env.getFolder());
                    }
                }
        ));
    }
}
