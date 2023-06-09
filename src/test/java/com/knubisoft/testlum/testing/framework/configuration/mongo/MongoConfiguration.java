package com.knubisoft.testlum.testing.framework.configuration.mongo;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.condition.OnMongoEnabledCondition;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Mongo;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Conditional({OnMongoEnabledCondition.class})
public class MongoConfiguration {

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
                MongoClient mongoClient = createMongoClient(mongo);
                MongoDatabase mongoDatabase = mongoClient.getDatabase(mongo.getDatabase());
                databaseMap.put(new AliasEnv(mongo.getAlias(), env), mongoDatabase);
            }
        }
    }

    private MongoClient createMongoClient(final Mongo mongo) {
        MongoCredential credential = MongoCredential.createCredential(
                mongo.getUsername(), mongo.getDatabase(), mongo.getPassword().toCharArray());
        ConnectionString url = new ConnectionString(mongo.getConnectionUrl());

        MongoClientSettings settings = MongoClientSettings.builder()
                .credential(credential)
                .applyConnectionString(url)
                .build();
        return MongoClients.create(settings);
    }
}
