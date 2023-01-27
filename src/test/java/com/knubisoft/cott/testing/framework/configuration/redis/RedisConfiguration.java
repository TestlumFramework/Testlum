package com.knubisoft.cott.testing.framework.configuration.redis;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnRedisEnabledCondition;
import com.knubisoft.cott.testing.model.global_config.Redis;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.UNDERSCORE;

@Configuration
@Conditional({OnRedisEnabledCondition.class})
public class RedisConfiguration {

    @Bean
    public Map<String, RedisStandaloneConfiguration> redisStandaloneConfiguration() {
        final Map<String, RedisStandaloneConfiguration> redisIntegration = new HashMap<>();
        GlobalTestConfigurationProvider.getIntegrations()
                .forEach(((s, integrations) -> addStandaloneConfig(s, integrations.getRedisIntegration().getRedis(),
                        redisIntegration)));
        return redisIntegration;
    }

    private void addStandaloneConfig(final String envName,
                                     final List<Redis> redises,
                                     final Map<String, RedisStandaloneConfiguration> redisIntegration) {
        for (Redis redis : redises) {
            if (redis.isEnabled()) {
                redisIntegration.put(envName + UNDERSCORE + redis.getAlias(),
                        new RedisStandaloneConfiguration(redis.getHost(), redis.getPort()));
            }
        }
    }

    @Bean("redisConnectionFactory")
    public Map<String, JedisConnectionFactory> jedisConnectionFactory(
            final Map<String, RedisStandaloneConfiguration> redisStandaloneConfiguration) {
        return redisStandaloneConfiguration.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> new JedisConnectionFactory(e.getValue())));
    }

    @Bean
    public Map<String, StringRedisConnection> stringRedisConnection(
            @Autowired @Qualifier("redisConnectionFactory")
            final Map<String, JedisConnectionFactory> redisConnectionFactory) {
        final Map<String, StringRedisConnection> connections = new HashMap<>();
        for (Map.Entry<String, JedisConnectionFactory> entry : redisConnectionFactory.entrySet()) {
            RedisConnection connection = entry.getValue().getConnection();
            connections.put(entry.getKey(), new DefaultStringRedisConnection(connection));
        }
        return connections;
    }
}
