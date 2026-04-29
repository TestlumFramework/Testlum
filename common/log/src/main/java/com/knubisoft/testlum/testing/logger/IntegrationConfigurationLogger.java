package com.knubisoft.testlum.testing.logger;

import com.knubisoft.testlum.log.Color;
import com.knubisoft.testlum.log.table.DynamicTableBuilder;
import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class IntegrationConfigurationLogger {

    private static final List<Method> CONTAINER_GETTERS = discoverContainerGetters();

    public void appendEnvironmentSections(final DynamicTableBuilder table,
                                          final String environment,
                                          final Integrations integrations) {
        if (integrations == null) {
            return;
        }
        List<Integration> instances = collectIntegrations(integrations);
        if (instances.isEmpty()) {
            return;
        }
        writeEnvironmentHeader(table, environment);
        instances.forEach(i ->
                table.row(computeRowColor(i.isEnabled()), i.getClass().getSimpleName(), i.getAlias(), i.isEnabled()));
    }

    private List<Integration> collectIntegrations(final Integrations integrations) {
        List<Integration> result = new ArrayList<>();
        for (Method getter : CONTAINER_GETTERS) {
            Object container = invokeGetter(getter, integrations);
            if (container != null) {
                collectFromContainer(container, result);
            }
        }
        return result;
    }

    private void collectFromContainer(final Object container, final List<Integration> out) {
        for (Method method : container.getClass().getMethods()) {
            if (method.getParameterCount() != 0 || !List.class.isAssignableFrom(method.getReturnType())) {
                continue;
            }
            Object value = invokeGetter(method, container);
            if (value instanceof List<?> list) {
                addIntegrationsFromList(list, out);
            }
        }
    }

    private void addIntegrationsFromList(final List<?> list, final List<Integration> out) {
        for (Object item : list) {
            if (item instanceof Integration integration) {
                out.add(integration);
            }
        }
    }

    private Object invokeGetter(final Method method, final Object target) {
        try {
            return method.invoke(target);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to invoke " + method, e);
        }
    }

    private void writeEnvironmentHeader(final DynamicTableBuilder table, final String environment) {
        table.columns(null, null, String.format(LogMessage.INTEGRATION_CONFIG_TABLE_ENV_ROW, environment));
        table.row(LogMessage.INTEGRATION_CONFIG_TABLE_NAME_HEADER,
                LogMessage.INTEGRATION_CONFIG_TABLE_ALIAS_HEADER,
                LogMessage.INTEGRATION_CONFIG_TABLE_ENABLED_HEADER);
    }

    private Color computeRowColor(final boolean isEnabled) {
        if (isEnabled) {
            return Color.GREEN;
        }
        return Color.RED;
    }

    private static List<Method> discoverContainerGetters() {
        return Arrays.stream(Integrations.class.getMethods())
                .filter(IntegrationConfigurationLogger::isContainerGetter)
                .sorted(Comparator.comparing(Method::getName))
                .toList();
    }

    private static boolean isContainerGetter(final Method method) {
        return method.getParameterCount() == 0
                && method.getName().startsWith("get")
                && Objects.equals(method.getReturnType().getPackage(), Integrations.class.getPackage());
    }

}
