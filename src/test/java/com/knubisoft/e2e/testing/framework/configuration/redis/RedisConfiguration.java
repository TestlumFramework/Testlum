package com.knubisoft.e2e.testing.framework.configuration.redis;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.configuration.condition.OnRedisEnabledCondition;
import com.knubisoft.e2e.testing.model.global_config.Redis;
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
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@Conditional({OnRedisEnabledCondition.class})
public class RedisConfiguration {
    @Bean
    public Map<String, RedisStandaloneConfiguration> redisStandaloneConfiguration() {
        final Map<String, RedisStandaloneConfiguration> properties = new HashMap<>();
        for (Redis redis : GlobalTestConfigurationProvider.getIntegrations().getRedises().getRedis()) {
            if (redis.isEnabled()) {
                properties.put(redis.getAlias(), new RedisStandaloneConfiguration(redis.getHost(), redis.getPort()));
            }
        }
        return properties;
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
