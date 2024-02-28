package com.uefa.platform.service.b2bpush.core.configuration;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import org.springframework.cloud.aws.messaging.config.QueueMessageHandlerFactory;
import org.springframework.cloud.aws.messaging.config.SimpleMessageListenerContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.PayloadMethodArgumentResolver;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Collections;

/**
 * Configuration that overrides {@link QueueMessageHandlerFactory}
 */
@Configuration
public class QueueMessageHandlerConfiguration {

    /**
     * Configures Springs generic queue message handler.
     * Allows messages payloads to be parsed by the {@link MappingJackson2MessageConverter} without a proper Mime-Type,
     * via setting {@link MappingJackson2MessageConverter#setStrictContentTypeMatch} to false, since AWS SQS does not send a Mime-Type
     *
     * @return configured {@link QueueMessageHandlerFactory}
     */
    @Bean
    public QueueMessageHandlerFactory queueMessageHandlerFactory() {
        QueueMessageHandlerFactory factory = new QueueMessageHandlerFactory();
        MappingJackson2MessageConverter messageConverter = new MappingJackson2MessageConverter();
        //set strict content type match to false
        messageConverter.setStrictContentTypeMatch(false);
        factory.setArgumentResolvers(Collections.singletonList(new PayloadMethodArgumentResolver(messageConverter, null, false)));
        return factory;
    }

    @Bean
    public SimpleMessageListenerContainerFactory simpleMessageListenerContainerFactory(AmazonSQSAsync amazonSqs) {
        SimpleMessageListenerContainerFactory factory = new SimpleMessageListenerContainerFactory();
        factory.setAmazonSqs(amazonSqs);
        factory.setMaxNumberOfMessages(1);
        factory.setTaskExecutor(taskExecutor());
        factory.setBackOffTime(1000L);

        return factory;
    }

    @Bean
    public AsyncTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setThreadNamePrefix("SQSListener - ");
        threadPoolTaskExecutor.setCorePoolSize(150);
        threadPoolTaskExecutor.setMaxPoolSize(300);
        threadPoolTaskExecutor.setQueueCapacity(8);
        threadPoolTaskExecutor.afterPropertiesSet();

        return threadPoolTaskExecutor;
    }
}
