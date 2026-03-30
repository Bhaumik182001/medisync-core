package com.bhaumik18.medisync_core.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing
public class MongoConfig {

    @Value("${spring.data.mongodb.username}")
    private String username;

    @Value("${spring.data.mongodb.password}")
    private String password;

    @Value("${spring.data.mongodb.database}")
    private String database;

    @Value("${spring.data.mongodb.host}")
    private String host;

    @Value("${spring.data.mongodb.port}")
    private String port;

    @Value("${spring.data.mongodb.authentication-database}")
    private String authSource;

    @Bean
    public MongoClient mongoClient() {
        // Now it's 100% dynamic, but still perfectly safe from the WARP bug
        // because the YAML explicitly passes 127.0.0.1
        String uri = String.format("mongodb://%s:%s@%s:%s/%s?authSource=%s", 
                username, password, host, port, database, authSource);
        
        ConnectionString connectionString = new ConnectionString(uri);
        
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        
        return MongoClients.create(mongoClientSettings);
    }
}
