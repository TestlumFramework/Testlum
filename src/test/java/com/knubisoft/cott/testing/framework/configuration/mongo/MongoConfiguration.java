package com.knubisoft.cott.testing.framework.configuration.mongo;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnMongoEnabledCondition;
import com.knubisoft.cott.testing.framework.env.AliasEnv;
import com.knubisoft.cott.testing.model.global_config.Integrations;
import com.knubisoft.cott.testing.model.global_config.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@Conditional({OnMongoEnabledCondition.class})
public class MongoConfiguration {

    @Bean
    public Map<AliasEnv, MongoDatabase> mongoDatabase(final Map<AliasEnv, MongoClient> mongoClient) {
        return mongoClient.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> entry.getValue().getDatabase(entry.getKey().getAlias())));
    }

    @Bean
    public Map<AliasEnv, MongoClient> mongoClient() {
        final Map<AliasEnv, MongoClient> clients = new HashMap<>();
        GlobalTestConfigurationProvider.getIntegrations()
                .forEach((env, integrations) -> addMongoClient(integrations, env, clients));
        return clients;
    }

    private void addMongoClient(final Integrations integrations,
                                final String env,
                                final Map<AliasEnv, MongoClient> clients) {
        for (Mongo mongo : integrations.getMongoIntegration().getMongo()) {
            if (mongo.isEnabled()) {
                MongoClient mongoClient = createMongoClient(mongo);
                clients.put(new AliasEnv(mongo.getAlias(), env), mongoClient);
            }
        }
    }

    private MongoClient createMongoClient(final Mongo mongo) {
        ServerAddress mongoAddress = new ServerAddress(mongo.getHost(), mongo.getPort());
        MongoCredential credential = MongoCredential.createCredential(
                mongo.getUsername(), mongo.getAuthenticationDatabase(), mongo.getPassword().toCharArray());
        MongoClientOptions mongoClientOptions = MongoClientOptions.builder().build();
        return new MongoClient(mongoAddress, credential, mongoClientOptions);
    }
}
