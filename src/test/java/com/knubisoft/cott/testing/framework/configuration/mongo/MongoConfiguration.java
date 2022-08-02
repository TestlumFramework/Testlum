package com.knubisoft.cott.testing.framework.configuration.mongo;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnMongoEnabledCondition;
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
    public Map<String, MongoClient> mongoClient() {
        final Map<String, MongoClient> clients = new HashMap<>();
        for (Mongo mongo : GlobalTestConfigurationProvider.getIntegrations().getMongoIntegration().getMongo()) {
            if (mongo.isEnabled()) {
                createMongoClientAndPutIntoMap(clients, mongo);
            }
        }
        return clients;
    }

    private void createMongoClientAndPutIntoMap(final Map<String, MongoClient> clients, final Mongo mongo) {
        ServerAddress mongoAddress = new ServerAddress(mongo.getHost(), mongo.getPort());
        MongoCredential credential = MongoCredential.createCredential(mongo.getUsername(),
                mongo.getAuthenticationDatabase(),
                mongo.getPassword().toCharArray());
        MongoClientOptions mongoClientOptions = MongoClientOptions.builder().build();
        clients.put(mongo.getAlias(), new MongoClient(mongoAddress, credential, mongoClientOptions));
    }

    @Bean
    public Map<String, MongoDatabase> mongoDatabase(final Map<String, MongoClient> mongoClient) {
        return mongoClient.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().getDatabase(e.getKey())
                ));
    }

}
