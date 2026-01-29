package com.knubisoft.testlum.testing.framework.configuration.mongo;

import com.knubisoft.testlum.testing.framework.configuration.condition.OnMongoEnabledCondition;
import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.connection.ConnectionTemplate;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Mongo;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import lombok.RequiredArgsConstructor;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.BsonValue;
import org.bson.conversions.Bson;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
@Conditional({OnMongoEnabledCondition.class})
@RequiredArgsConstructor
public class MongoConfiguration {

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
                        "MongoDB - " + mongo.getAlias(),
                        () -> {
                            MongoClient mongoClient = createMongoClient(mongo);
                            try {
                                Bson ping = new BsonDocument("ping", new BsonInt64(1));
                                mongoClient.getDatabase(mongo.getDatabase()).runCommand(ping);

                                return mongoClient.getDatabase(mongo.getDatabase());
                            } catch (Exception e) {
                                mongoClient.close();
                                throw new DefaultFrameworkException(e.getMessage());
                            }
                        }
                );

                databaseMap.put(new AliasEnv(mongo.getAlias(), env), resilientDb);
            }
        }
    }

    private MongoClient createMongoClient(final Mongo mongo) {
        MongoCredential credential = MongoCredential.createCredential(
                mongo.getUsername(), mongo.getDatabase(), mongo.getPassword().toCharArray());
        ServerAddress mongoAddress = new ServerAddress(mongo.getHost(), mongo.getPort());

        MongoClientSettings settings = MongoClientSettings.builder()
                .credential(credential)
                .applyToClusterSettings(builder -> builder
                        .hosts(Collections.singletonList(mongoAddress))
                        .serverSelectionTimeout(5000, TimeUnit.MILLISECONDS))
                .applyToSocketSettings(builder -> builder
                        .connectTimeout(5000, TimeUnit.MILLISECONDS)
                        .readTimeout(5000, TimeUnit.MILLISECONDS))
                .build();
        return MongoClients.create(settings);
    }
}
