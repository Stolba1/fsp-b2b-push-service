package com.uefa.platform.service.b2bpush.core.domain.feed.data.entity;

import java.util.Map;
import java.util.Objects;

public class ProcessStaticFeedResult {

    private String data;
    private String hash;
    private Map<String, String> parameters;
    private String finalUrl;

    public ProcessStaticFeedResult(String data, String hash, Map<String, String> parameters, String finalUrl) {
        this.data = data;
        this.hash = hash;
        this.parameters = parameters;
        this.finalUrl = finalUrl;
    }

    public String getData() {
        return data;
    }

    public String getHash() {
        return hash;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public String getFinalUrl() {
        return finalUrl;
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProcessStaticFeedResult that = (ProcessStaticFeedResult) o;
        return Objects.equals(data, that.data) && Objects.equals(hash, that.hash) && Objects.equals(parameters, that.parameters)
                && Objects.equals(finalUrl, that.finalUrl);
    }

    @Override public int hashCode() {
        return Objects.hash(data, hash, parameters, finalUrl);
    }

    @Override public String toString() {
        return "ProcessStaticFeedResult{" +
                "data='" + data + '\'' +
                ", hash='" + hash + '\'' +
                ", parameters=" + parameters +
                ", finalUrl=" + finalUrl +
                '}';
    }
}
