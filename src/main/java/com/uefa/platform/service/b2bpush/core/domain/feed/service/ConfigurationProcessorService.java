package com.uefa.platform.service.b2bpush.core.domain.feed.service;

import com.uefa.platform.dto.message.CommandMessage;
import com.uefa.platform.dto.message.PlatformMessage;
import com.uefa.platform.dto.message.b2b.CommandData;
import com.uefa.platform.service.b2bpush.core.domain.feed.FeedMessageProducer;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Client;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Feed;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.FeedConfigurationEntity;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.ProcessStaticFeedResult;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

import static com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.LiveFeedDataType.getRoutingKey;

@Service
public class ConfigurationProcessorService {

    private static final String B2B_ROUTING_KEY = "B2B-PUSH";
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationProcessorService.class);

    private final FeedHttpClient feedHttpClient;

    private final FeedMessageProducer feedMessageProducer;

    @Autowired
    public ConfigurationProcessorService(FeedHttpClient feedHttpClient,
                                         FeedMessageProducer feedMessageProducer) {
        this.feedHttpClient = feedHttpClient;
        this.feedMessageProducer = feedMessageProducer;
    }

    public void processLiveFeed(Feed feed, Client.FeedConfiguration config, Map<String, String> bootstrapOptionalParameters) {
        Map<String, String> params = config.getParameters().stream()
                .collect(Collectors.toMap(Client.FeedConfiguration.Parameter::getName, Client.FeedConfiguration.Parameter::getValue));
        params.putAll(bootstrapOptionalParameters);
        String routingKey = getRoutingKey(feed.getLiveDataType());
        CommandMessage commandMessage = new CommandMessage(PlatformMessage.Provider.B2B_PUSH,
                PlatformMessage.Type.COMMAND,
                new CommandData(B2B_ROUTING_KEY,
                        config.getId(),
                        params,
                        CommandData.Type.BOOTSTRAP,
                        CommandData.CommandDetails.valueOf(feed.getLiveDataType().name())));
        feedMessageProducer.sendCommand(commandMessage, routingKey);
    }

    public ProcessStaticFeedResult processStaticFeed(FeedConfigurationEntity configurationEntity) {
        String finalURL = buildUrlWithParams(configurationEntity.getFeedUrl(), configurationEntity.getParameters());
        if (finalURL == null) {
            //error occurred while building the final url
            return null;
        }

        final String feedData = feedHttpClient.getFeedResult(finalURL);
        if (feedData == null) {
            return null;
        }

        //build hash
        String hash = HashUtil.getMD5(feedData);

        return new ProcessStaticFeedResult(feedData, hash, configurationEntity.getParameters(), finalURL);
    }

    private String buildUrlWithParams(String urlTemplate, Map<String, String> params) {
        final String url = StringSubstitutor.replace(urlTemplate, params, "{", "}");
        if (url.contains("{")) {
            LOGGER.error("Feed URL build failed, not all parameters are configured. URL: {}, parameters {}", urlTemplate, params);
            return null;
        }
        return url;
    }

}
