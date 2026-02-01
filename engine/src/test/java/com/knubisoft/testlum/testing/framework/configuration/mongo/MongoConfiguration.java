package com.knubisoft.testlum.testing.framework.configuration.mongo;

import com.knubisoft.testlum.testing.framework.configuration.condition.OnMongoEnabledCondition;
import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.connection.ConnectionTemplate;
import com.knubisoft.testlum.testing.framework.configuration.connection.health.HealthCheckFactory;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Mongo;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.CONNECTION_INTEGRATION_DATA;

@Configuration
@Conditional({OnMongoEnabledCondition.class})
@RequiredArgsConstructor
public class MongoConfiguration {

    private static final int TIMEOUT = 5000;

    private final ConnectionTemplate connectionTemplate;

    @Bean
    public Map<AliasEnv, MongoDatabase> mongoDatabases() {
        final Map<AliasEnv, MongoDatabase> databaseMap = new HashMap<>();
        GlobalTestConfigurationProvider.getIntegrations()
                .forEach((env, integrations) -> addMongoDatabase(integrations, env, databaseMap));
        return databaseMap;
    }

    private void addMongoDatabase(final Integrations integrations,
                                  final String env,
                                  final Map<AliasEnv, MongoDatabase> databaseMap) {
        for (Mongo mongo : integrations.getMongoIntegration().getMongo()) {
            if (mongo.isEnabled()) {
                MongoDatabase resilientDb = connectionTemplate.executeWithRetry(
                        String.format(CONNECTION_INTEGRATION_DATA, "MongoDB", mongo.getAlias()),
                        () -> createMongoClient(mongo).getDatabase(mongo.getDatabase()),
                        HealthCheckFactory.forMongoDb(mongo)
                );
                databaseMap.put(new AliasEnv(mongo.getAlias(), env), resilientDb);
            }
        }
    }

    private MongoClient createMongoClient(final Mongo mongo) {
        MongoCredential credential = MongoCredential.createCredential(
                mongo.getUsername(), mongo.getDatabase(), mongo.getPassword().toCharArray());
        ServerAddress mongoAddress = new ServerAddress(mongo.getHost(), mongo.getPort());

        MongoClientSettings settings = MongoClientSettings.builder().credential(credential)
                .applyToClusterSettings(builder -> builder
                        .hosts(Collections.singletonList(mongoAddress))
                        .serverSelectionTimeout(TIMEOUT, TimeUnit.MILLISECONDS))
                .applyToSocketSettings(builder -> builder
                        .connectTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                        .readTimeout(TIMEOUT, TimeUnit.MILLISECONDS))
                .build();
        return MongoClients.create(settings);
    }
}
