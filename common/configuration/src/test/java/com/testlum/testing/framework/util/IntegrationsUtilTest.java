package com.testlum.testing.framework.util;

import com.testlum.testing.framework.EnvToIntegrationMap;
import com.testlum.testing.framework.env.AliasEnv;
import com.testlum.testing.framework.exception.IntegrationDisabledException;
import com.testlum.testing.model.global_config.Api;
import com.testlum.testing.model.global_config.AuthStrategy;
import com.testlum.testing.model.global_config.AuthStrategies;
import com.testlum.testing.model.global_config.BasicAuthStrategy;
import com.testlum.testing.model.global_config.JwtAuthStrategy;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.model.global_config.Apis;
import com.testlum.testing.model.global_config.Integrations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class IntegrationsUtilTest {

    private IntegrationsUtil integrationsUtil;

    private Api createApi(final String alias, final boolean enabled) {
        final Api api = new Api();
        api.setAlias(alias);
        api.setEnabled(enabled);
        api.setUrl("http://localhost");
        return api;
    }

    @BeforeEach
    void setUp() {
        final Integrations integrations = new Integrations();
        final Apis apis = new Apis();
        apis.getApi().add(createApi("DEFAULT", true));
        apis.getApi().add(createApi("secondary", true));
        apis.getApi().add(createApi("disabled", false));
        integrations.setApis(apis);

        final EnvToIntegrationMap envMap = new EnvToIntegrationMap(Map.of("dev", integrations));
        integrationsUtil = new IntegrationsUtil(envMap);
    }

    @Nested
    class FindForAliasEnv {
        @Test
        void findsApiByAliasAndEnv() {
            final AliasEnv aliasEnv = new AliasEnv("DEFAULT", "dev");
            final Api result = integrationsUtil.findForAliasEnv(Api.class, aliasEnv);
            assertNotNull(result);
            assertEquals("DEFAULT", result.getAlias());
        }

        @Test
        void throwsForDisabledAlias() {
            final AliasEnv aliasEnv = new AliasEnv("disabled", "dev");
            assertThrows(IntegrationDisabledException.class,
                    () -> integrationsUtil.findForAliasEnv(Api.class, aliasEnv));
        }

        @Test
        void throwsForNonExistentAlias() {
            final AliasEnv aliasEnv = new AliasEnv("nonexistent", "dev");
            assertThrows(IntegrationDisabledException.class,
                    () -> integrationsUtil.findForAliasEnv(Api.class, aliasEnv));
        }
    }

    @Nested
    class FindListByEnv {
        @Test
        void returnsListForExistingEnv() {
            final List<Api> result = integrationsUtil.findListByEnv(Api.class, "dev");
            assertEquals(3, result.size());
        }

        @Test
        void returnsEmptyListForMissingEnv() {
            final List<Api> result = integrationsUtil.findListByEnv(Api.class, "nonexistent");
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class FindForAlias {
        @Test
        void findsEnabledIntegrationByAlias() {
            final List<Api> apis = integrationsUtil.findListByEnv(Api.class, "dev");
            final Api result = integrationsUtil.findForAlias(apis, "secondary");
            assertEquals("secondary", result.getAlias());
        }

        @Test
        void throwsForDisabledIntegration() {
            final List<Api> apis = integrationsUtil.findListByEnv(Api.class, "dev");
            assertThrows(IntegrationDisabledException.class,
                    () -> integrationsUtil.findForAlias(apis, "disabled"));
        }

        @Test
        void nullAliasDefaultsToDEFAULT() {
            final List<Api> apis = integrationsUtil.findListByEnv(Api.class, "dev");
            final Api result = integrationsUtil.findForAlias(apis, null);
            assertEquals("DEFAULT", result.getAlias());
        }
    }

    @Nested
    class FindApiForAlias {
        @Test
        void findsApiByAlias() {
            final List<Api> apis = integrationsUtil.findListByEnv(Api.class, "dev");
            final Api result = integrationsUtil.findApiForAlias(apis, "DEFAULT");
            assertNotNull(result);
        }

        @Test
        void throwsForMissingApi() {
            final List<Api> apis = integrationsUtil.findListByEnv(Api.class, "dev");
            assertThrows(IntegrationDisabledException.class,
                    () -> integrationsUtil.findApiForAlias(apis, "ghost"));
        }
    }

    @Nested
    class IsEnabled {
        @Test
        void returnsTrueWhenAnyEnabled() {
            final List<Api> apis = integrationsUtil.findListByEnv(Api.class, "dev");
            assertTrue(integrationsUtil.isEnabled(apis));
        }

        @Test
        void returnsFalseWhenNoneEnabled() {
            final Api disabledApi = createApi("only", false);
            assertFalse(integrationsUtil.isEnabled(List.of(disabledApi)));
        }

        @Test
        void returnsFalseForEmptyList() {
            assertFalse(integrationsUtil.isEnabled(Collections.emptyList()));
        }
    }

    @Nested
    class FindAuthStrategy {
        @Test
        void resolvesAliasFromSelectedEnvironment() {
            BasicAuthStrategy dev = basic("BASIC");
            BasicAuthStrategy ci = basic("BASIC");
            IntegrationsUtil util = new IntegrationsUtil(new EnvToIntegrationMap(Map.of(
                    "dev", authRegistry(dev), "ci", authRegistry(ci))));
            assertSame(ci, util.findAuthStrategy(BasicAuthStrategy.class, new AliasEnv("BASIC", "ci")));
            assertSame(dev, util.findAuthStrategy(AuthStrategy.class, new AliasEnv("BASIC", "dev")));
        }

        @Test
        void nullAliasResolvesConfiguredDefault() {
            BasicAuthStrategy expected = basic("DEFAULT");
            IntegrationsUtil util = authUtil(expected);
            assertSame(expected, util.findAuthStrategy(AuthStrategy.class, new AliasEnv(null, "dev")));
        }

        @Test
        void missingEnvironmentAndMissingAliasFailClearly() {
            IntegrationsUtil util = authUtil(basic("BASIC"));
            assertThrows(DefaultFrameworkException.class,
                    () -> util.findAuthStrategy(AuthStrategy.class, new AliasEnv("BASIC", "missing")));
            assertThrows(DefaultFrameworkException.class,
                    () -> util.findAuthStrategy(AuthStrategy.class, new AliasEnv("missing", "dev")));
        }

        @Test
        void missingRegistryFailsClearly() {
            assertThrows(DefaultFrameworkException.class,
                    () -> integrationsUtil.findAuthStrategy(AuthStrategy.class, new AliasEnv("DEFAULT", "dev")));
        }

        @Test
        void mismatchedRequestedTypeFailsClearly() {
            IntegrationsUtil util = authUtil(basic("BASIC"));
            assertThrows(DefaultFrameworkException.class,
                    () -> util.findAuthStrategy(JwtAuthStrategy.class, new AliasEnv("BASIC", "dev")));
        }

        @Test
        void duplicateAliasAcrossTypesIsNotHiddenByRequestedType() {
            JwtAuthStrategy jwt = new JwtAuthStrategy();
            jwt.setAlias("SAME");
            IntegrationsUtil util = authUtil(basic("SAME"), jwt);
            assertThrows(DefaultFrameworkException.class,
                    () -> util.findAuthStrategy(BasicAuthStrategy.class, new AliasEnv("SAME", "dev")));
        }
    }

    private BasicAuthStrategy basic(final String alias) {
        BasicAuthStrategy strategy = new BasicAuthStrategy();
        strategy.setAlias(alias);
        return strategy;
    }

    private Integrations authRegistry(final AuthStrategy... strategies) {
        Integrations config = new Integrations();
        AuthStrategies registry = new AuthStrategies();
        registry.getJwtOrBasicOrCustom().addAll(List.of(strategies));
        config.setAuthStrategies(registry);
        return config;
    }

    private IntegrationsUtil authUtil(final AuthStrategy... strategies) {
        return new IntegrationsUtil(new EnvToIntegrationMap(Map.of("dev", authRegistry(strategies))));
    }
}
