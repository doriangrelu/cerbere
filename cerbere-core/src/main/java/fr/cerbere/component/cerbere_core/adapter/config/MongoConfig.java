package fr.cerbere.component.cerbere_core.adapter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;

@Configuration
public class MongoConfig {

    @Bean
    public MongoTransactionManager transactionManager(final MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }

}
