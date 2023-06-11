package com.knubisoft.testlum.testing.framework.configuration.mongo;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.condition.OnMongoEnabledCondition;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Mongo;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.connection.ClusterConnectionMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Conditional({OnMongoEnabledCondition.class})
public class MongoConfiguration {

    @Bean
    public Map<AliasEnv, MongoTemplate> mongoDatabases() {
        final Map<AliasEnv, MongoTemplate> databaseMap = new HashMap<>();
        GlobalTestConfigurationProvider.getIntegrations()
                .forEach((env, integrations) -> addMongoDatabase(integrations, env, databaseMap));
        return databaseMap;
    }

    private void addMongoDatabase(final Integrations integrations,
                                  final String env,
                                  final Map<AliasEnv, MongoTemplate> databaseMap) {
        for (Mongo mongo : integrations.getMongoIntegration().getMongo()) {
            if (mongo.isEnabled()) {
                MongoClient mongoClient = createMongoClient(mongo);
                MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, mongo.getDatabase());
//                MongoDatabase mongoDatabase = mongoClient.getDatabase(mongo.getDatabase());
                databaseMap.put(new AliasEnv(mongo.getAlias(), env), mongoTemplate);
            }
        }
    }

    private MongoClient createMongoClient(final Mongo mongo) {
        MongoCredential credential = MongoCredential.createCredential(
                mongo.getUsername(), mongo.getDatabase(), mongo.getPassword().toCharArray());
        ServerAddress mongoAddress = new ServerAddress(mongo.getHost(), mongo.getPort());

        MongoClientSettings settings = MongoClientSettings.builder()
                .credential(credential)
                .applyToClusterSettings(builder ->
                        builder.mode(ClusterConnectionMode.SINGLE).hosts(Collections.singletonList(mongoAddress)))
                .build();
        return MongoClients.create(settings);
    }
}
