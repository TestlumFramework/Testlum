package com.knubisoft.testlum.testing.framework.configuration.redis;

import com.knubisoft.testlum.testing.framework.configuration.condition.OnRedisEnabledCondition;
import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Redis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.DefaultStringRedisConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@Conditional({OnRedisEnabledCondition.class})
public class RedisConfiguration {

    @Bean
    public Map<AliasEnv, StringRedisConnection> stringRedisConnection(
            @Autowired @Qualifier("redisConnectionFactory")
            final Map<AliasEnv, JedisConnectionFactory> redisConnectionFactory) {
        final Map<AliasEnv, StringRedisConnection> redisConnectionMap = new HashMap<>();
        redisConnectionFactory.forEach((aliasEnv, jedisConnectionFactory) -> {
            RedisConnection connection = jedisConnectionFactory.getConnection();
            redisConnectionMap.put(aliasEnv, new DefaultStringRedisConnection(connection, new StringRedisSerializer()));
        });
        return redisConnectionMap;
    }

    @Bean("redisConnectionFactory")
    public Map<AliasEnv, JedisConnectionFactory> jedisConnectionFactory(
            final Map<AliasEnv, RedisStandaloneConfiguration> redisStandaloneConfiguration) {
        return redisStandaloneConfiguration.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> new JedisConnectionFactory(e.getValue())));
    }

    @Bean
    public Map<AliasEnv, RedisStandaloneConfiguration> redisStandaloneConfiguration() {
        final Map<AliasEnv, RedisStandaloneConfiguration> redisConfigMap = new HashMap<>();
        GlobalTestConfigurationProvider.getIntegrations()
                .forEach((env, integrations) -> addStandaloneConfig(integrations, env, redisConfigMap));
        return redisConfigMap;
    }

    private void addStandaloneConfig(final Integrations integrations,
                                     final String env,
                                     final Map<AliasEnv, RedisStandaloneConfiguration> redisConfigMap) {
        for (Redis redis : integrations.getRedisIntegration().getRedis()) {
            if (redis.isEnabled()) {
                RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(
                        redis.getHost(), redis.getPort());
                redisConfigMap.put(new AliasEnv(redis.getAlias(), env), redisStandaloneConfiguration);
            }
        }
    }
}
