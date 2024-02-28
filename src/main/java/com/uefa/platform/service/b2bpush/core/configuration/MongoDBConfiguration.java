package com.uefa.platform.service.b2bpush.core.configuration;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.util.Assert;

@Configuration
@EnableMongoRepositories(basePackages = {
        "com.uefa.platform.service.b2bpush.core.domain.archive",
        "com.uefa.platform.service.b2bpush.core.domain.feed.data"
})
public class MongoDBConfiguration extends AbstractMongoClientConfiguration {

    private final String connectionString;

    private final String databaseName;

    public MongoDBConfiguration(@Value("${mongodb.connection.string}") String connectionString,
                                @Value("${mongodb.databasename}") String databaseName) {
        this.connectionString = connectionString;
        this.databaseName = databaseName;
    }

    @NotNull
    @Override
    protected String getDatabaseName() {
        return databaseName;
    }

    @NotNull
    @Override
    @Bean
    public MongoClient mongoClient() {
        Assert.isTrue(connectionString != null && !connectionString.isEmpty(), "There must be mongo connection string specified!");
        return MongoClients.create(new ConnectionString(connectionString));
    }

    /**
     * Configure whether to automatically create indices for domain types by deriving the
     * {@link IndexDefinition} from the entity or not.
     *
     * @return {@literal false} by default. <br />
     * <strong>INFO</strong>: As of 3.x the default is set to {@literal false}; In 2.x it was {@literal true}.
     * @since 2.2
     */
    @Override
    protected boolean autoIndexCreation() {
        return true;
    }

}
