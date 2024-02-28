package com.uefa.platform.service.b2bpush.core.domain.feed.data.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.Objects;

@Document(collection = "hashes")
public class Hash {

    public static final String HASH_FIELD = "hash";

    public static final String LAST_UPDATED_FIELD = "lastUpdated";

    @Id
    private final HashIdentifier id;

    @Field(HASH_FIELD)
    private final String hash;

    @Field(LAST_UPDATED_FIELD)
    private final Instant lastUpdated;

    public Hash(HashIdentifier id, String hash, Instant lastUpdated) {
        this.id = id;
        this.hash = hash;
        this.lastUpdated = lastUpdated;
    }

    public HashIdentifier getId() {
        return id;
    }

    public String getHash() {
        return hash;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Hash hash1 = (Hash) o;
        return Objects.equals(id, hash1.id) && Objects.equals(hash, hash1.hash) && Objects.equals(lastUpdated, hash1.lastUpdated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, hash, lastUpdated);
    }

    @Override
    public String toString() {
        return "Hash{" +
                "id=" + id +
                ", hash='" + hash + '\'' +
                ", lastUpdated=" + lastUpdated +
                '}';
    }

    public static class HashIdentifier {
        public static final String URL_FIELD = "url";
        public static final String CONFIGURATION_ID_FIELD = "configurationId";
        public static final String CLIENT_ID_FIELD = "clientId";

        @Field(URL_FIELD)
        private final String url;

        @Field(CONFIGURATION_ID_FIELD)
        private final String configurationId;

        @Field(CLIENT_ID_FIELD)
        private final String clientId;

        public HashIdentifier(String url, String configurationId, String clientId) {
            this.url = url;
            this.configurationId = configurationId;
            this.clientId = clientId;
        }

        public String getUrl() {
            return url;
        }

        public String getConfigurationId() {
            return configurationId;
        }

        public String getClientId() {
            return clientId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            HashIdentifier that = (HashIdentifier) o;
            return Objects.equals(url, that.url) && Objects.equals(configurationId, that.configurationId) && Objects.equals(clientId, that.clientId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(url, configurationId, clientId);
        }

        @Override
        public String toString() {
            return "HashIdentifier{" +
                    "url='" + url + '\'' +
                    ", configurationId='" + configurationId + '\'' +
                    ", clientId='" + clientId + '\'' +
                    '}';
        }
    }
}
