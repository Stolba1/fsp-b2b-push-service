package com.uefa.platform.service.b2bpush.core.domain.feed.data.entity;

import java.util.Objects;
import java.util.TreeMap;

public class FeedConfigurationEntity {

    private String feedId;

    private String feedUrl;

    private TreeMap<String, String> parameters;

    private String feedCode;

    private String configurationId;

    private Boolean sharePayloadWithClient;


    public FeedConfigurationEntity(String feedId, String feedUrl, TreeMap<String, String> parameters, String feedCode,
                                   String configurationId, Boolean sharePayloadWithClient) {
        this.feedId = feedId;
        this.feedUrl = feedUrl;
        this.parameters = parameters;
        this.feedCode = feedCode;
        this.configurationId = configurationId;
        this.sharePayloadWithClient = sharePayloadWithClient;
    }

    public String getFeedId() {
        return feedId;
    }

    public TreeMap<String, String> getParameters() {
        return parameters;
    }

    public String getFeedUrl() {
        return feedUrl;
    }

    public String getFeedCode() {
        return feedCode;
    }

    public String getConfigurationId() {
        return configurationId;
    }

    public Boolean isSharePayloadWithClient() {
        return sharePayloadWithClient;
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FeedConfigurationEntity entity = (FeedConfigurationEntity) o;
        return Objects.equals(sharePayloadWithClient, entity.sharePayloadWithClient) && Objects.equals(feedId, entity.feedId) &&
                Objects.equals(feedUrl, entity.feedUrl) && Objects.equals(parameters, entity.parameters) &&
                Objects.equals(feedCode, entity.feedCode) && Objects.equals(configurationId, entity.configurationId);
    }

    @Override public int hashCode() {
        return Objects.hash(feedId, feedUrl, parameters, feedCode, configurationId, sharePayloadWithClient);
    }

    @Override public String toString() {
        return "FeedConfigurationEntity{" +
                "feedId='" + feedId + '\'' +
                ", feedUrl='" + feedUrl + '\'' +
                ", parameters=" + parameters +
                ", feedCode='" + feedCode + '\'' +
                ", configurationId='" + configurationId + '\'' +
                ", sharePayloadWithClient=" + sharePayloadWithClient +
                '}';
    }
}
