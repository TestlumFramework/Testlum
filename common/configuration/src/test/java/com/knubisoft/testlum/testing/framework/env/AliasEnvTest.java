package com.knubisoft.testlum.testing.framework.env;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class AliasEnvTest {

    @Nested
    class Construction {
        @Test
        void storesAliasAndEnvironment() {
            final AliasEnv aliasEnv = new AliasEnv("myAlias", "dev");
            assertEquals("myAlias", aliasEnv.getAlias());
            assertEquals("dev", aliasEnv.getEnvironment());
        }

        @Test
        void nullValuesAllowed() {
            final AliasEnv aliasEnv = new AliasEnv(null, null);
            assertEquals(null, aliasEnv.getAlias());
            assertEquals(null, aliasEnv.getEnvironment());
        }
    }

    @Nested
    class Equality {
        @Test
        void equalWhenSameAliasAndEnv() {
            final AliasEnv a = new AliasEnv("alias1", "prod");
            final AliasEnv b = new AliasEnv("alias1", "prod");
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        void notEqualWhenDifferent() {
            final AliasEnv a = new AliasEnv("alias1", "prod");
            final AliasEnv b = new AliasEnv("alias1", "dev");
            assertNotEquals(a, b);
        }
    }

    @Nested
    class ToStringTest {
        @Test
        void toStringContainsFields() {
            final AliasEnv aliasEnv = new AliasEnv("myAlias", "staging");
            final String str = aliasEnv.toString();
            assertEquals(true, str.contains("myAlias"));
            assertEquals(true, str.contains("staging"));
        }
    }
}
