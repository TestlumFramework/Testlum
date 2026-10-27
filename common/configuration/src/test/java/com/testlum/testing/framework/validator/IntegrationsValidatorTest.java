package com.testlum.testing.framework.validator;

import com.testlum.testing.framework.FileSearcher;
import com.testlum.testing.framework.TestResourceSettings;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.model.global_config.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link IntegrationsValidator} verifying alias uniqueness,
 * cross-environment consistency, and auth validation.
 */
class IntegrationsValidatorTest {

    private IntegrationsValidator validator;
    private FileSearcher fileSearcher;

    @BeforeEach
    void setUp() {
        fileSearcher = mock(FileSearcher.class);
        final TestResourceSettings settings = mock(TestResourceSettings.class);
        when(settings.getEnvConfigFolder()).thenReturn(new File("/test"));
        when(fileSearcher.searchFileFromEnvFolder(anyString(), anyString()))
                .thenReturn(Optional.of(new File("/test/integrations.xml")));
        validator = new IntegrationsValidator(fileSearcher, settings);
    }

    @Nested
    class ValidateEmptyMap {
        @Test
        void emptyMapDoesNotThrow() {
            assertDoesNotThrow(() -> validator.validate(Map.of()));
        }
    }

    @Nested
    class ValidateNoIntegrations {
        @Test
        void emptyIntegrationsDoNotThrow() {
            final Integrations env1 = new Integrations();
            final Integrations env2 = new Integrations();
            final Map<String, Integrations> map = new LinkedHashMap<>();
            map.put("dev", env1);
            map.put("staging", env2);
            assertDoesNotThrow(() -> validator.validate(map));
        }
    }

    @Nested
    class ValidateApiIntegrations {
        @Test
        void matchingApisAcrossEnvsDoNotThrow() {
            final Map<String, Integrations> map = new LinkedHashMap<>();
            map.put("dev", createIntegrationsWithApi("myApi"));
            map.put("staging", createIntegrationsWithApi("myApi"));
            assertDoesNotThrow(() -> validator.validate(map));
        }

        @Test
        void mismatchedApiAliasThrows() {
            final Map<String, Integrations> map = new LinkedHashMap<>();
            map.put("dev", createIntegrationsWithApi("api1"));
            map.put("staging", createIntegrationsWithApi("api2"));
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(map));
        }

        @Test
        void apiInOneEnvOnlyThrows() {
            final Map<String, Integrations> map = new LinkedHashMap<>();
            map.put("dev", createIntegrationsWithApi("api1"));
            map.put("staging", new Integrations());
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(map));
        }

        @Test
        void duplicateAliasInSameEnvThrows() {
            final Integrations integrations = new Integrations();
            final Apis apis = new Apis();
            apis.getApi().add(createApi("dup", true));
            apis.getApi().add(createApi("dup", true));
            integrations.setApis(apis);

            final Map<String, Integrations> map = new LinkedHashMap<>();
            map.put("dev", integrations);
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.validate(map));
        }
    }

    @Nested
    class ValidateAuthConsistency {
        @Test
        void matchingStrategiesWithDifferentEndpointsDoNotThrow() {
            assertDoesNotThrow(() -> validator.validate(Map.of(
                    "dev", authConfig(jwt("JWT", "http://dev/login")),
                    "staging", authConfig(jwt("JWT", "http://staging/login")))));
        }

        @Test
        void missingStrategyInAnotherEnvironmentThrows() {
            assertThrows(DefaultFrameworkException.class, () -> validator.validate(Map.of(
                    "dev", authConfig(jwt("JWT", "http://dev/login")), "staging", new Integrations())));
        }

        @Test
        void mismatchedStrategyTypeThrows() {
            BasicAuthStrategy basic = new BasicAuthStrategy();
            basic.setAlias("JWT");
            assertThrows(DefaultFrameworkException.class, () -> validator.validate(Map.of(
                    "dev", authConfig(jwt("JWT", "http://dev/login")), "staging", authConfig(basic))));
        }

        @Test
        void duplicateAliasesAcrossTypesThrowEvenInOneEnvironment() {
            BasicAuthStrategy basic = new BasicAuthStrategy();
            basic.setAlias("JWT");
            Integrations config = authConfig(jwt("JWT", "http://dev/login"), basic);
            assertThrows(DefaultFrameworkException.class, () -> validator.validate(Map.of("dev", config)));
        }

        @Test
        void mismatchedAliasesThrow() {
            assertThrows(DefaultFrameworkException.class, () -> validator.validate(Map.of(
                    "dev", authConfig(jwt("FIRST", "http://dev/login")),
                    "staging", authConfig(jwt("SECOND", "http://staging/login")))));
        }

        @Test
        void differentCustomClassesAcrossEnvironmentsDoNotThrow() {
            assertDoesNotThrow(() -> validator.validate(Map.of(
                    "local", authConfig(custom("example.CustomAuthLocal")),
                    "regression", authConfig(custom("example.CustomAuthRegression")))));
        }

        @Test
        void matchingCustomClassesDoNotThrow() {
            assertDoesNotThrow(() -> validator.validate(Map.of(
                    "dev", authConfig(custom("example.CustomAuth")),
                    "staging", authConfig(custom("example.CustomAuth")))));
        }
    }

    private Integrations authConfig(final AuthStrategy... strategies) {
        Integrations config = new Integrations();
        AuthStrategies registry = new AuthStrategies();
        registry.getJwtOrBasicOrCustom().addAll(java.util.List.of(strategies));
        config.setAuthStrategies(registry);
        return config;
    }

    private JwtAuthStrategy jwt(final String alias, final String endpoint) {
        JwtAuthStrategy strategy = new JwtAuthStrategy();
        strategy.setAlias(alias);
        strategy.setLoginEndpoint(endpoint);
        return strategy;
    }

    private CustomAuthStrategy custom(final String className) {
        CustomAuthStrategy strategy = new CustomAuthStrategy();
        strategy.setAlias("CUSTOM");
        strategy.setClassName(className);
        return strategy;
    }

    private Integrations createIntegrationsWithApi(final String alias) {
        final Integrations integrations = new Integrations();
        final Apis apis = new Apis();
        apis.getApi().add(createApi(alias, true));
        integrations.setApis(apis);
        return integrations;
    }

    private Api createApi(final String alias, final boolean enabled) {
        final Api api = new Api();
        api.setAlias(alias);
        api.setEnabled(enabled);
        return api;
    }
}
