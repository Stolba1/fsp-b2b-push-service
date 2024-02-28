package com.uefa.platform.service.b2bpush.core.domain.feed.listener;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.uefa.platform.service.b2bpush.core.domain.feed.service.FeedProcessorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * SQS Worker task listener that listens to sqs scheduled tasks
 */
@Component
public class SQSWorkerTaskListener {

    private static final Logger LOG = LoggerFactory.getLogger(SQSWorkerTaskListener.class);

    private final AsyncTaskExecutor taskExecutor;

    private final FeedProcessorService feedProcessorService;

    @Autowired
    public SQSWorkerTaskListener(AsyncTaskExecutor taskExecutor, FeedProcessorService feedProcessorService) {
        this.taskExecutor = taskExecutor;
        this.feedProcessorService = feedProcessorService;
    }

    /**
     * SQS receiver.
     *
     * @param task not null
     */
    @SqsListener("${sqs.tasks.queue.name}")
    public void receive(@Payload final Task task) {
        LOG.debug("Received task {}", task);

        // workaround for https://github.com/spring-cloud/spring-cloud-aws/issues/166
        CompletableFuture.supplyAsync(() -> {
            try {
                importData(task);
            } catch (Exception e) {
                LOG.error("Failed to process task {} \nError {}", task, e.getMessage(), e);
            }
            // we don't care about the return value, only about moving processing off-thread
            return null;
        }, taskExecutor);
    }

    public void importData(Task task) {
        if (Task.Type.PROCESS_FEEDS.equals(task.type)) {
            long start = System.currentTimeMillis();
            feedProcessorService.processStaticFeeds();
            long elapsed = System.currentTimeMillis() - start;
            LOG.debug("{} Task done in {} ms!", task.type, elapsed);
        } else {
            LOG.error("Message type: {} not supported", task.getType());
        }
    }

    /**
     * Object representation of a task json
     */
    public static class Task {

        private final Type type;

        /**
         * {@link JsonCreator} constructor to create {@link Task} object from json
         *
         * @param type not null
         */
        @JsonCreator
        public Task(@JsonProperty("type") Type type) {
            this.type = Objects.requireNonNull(type);
        }

        public Type getType() {
            return type;
        }

        @Override
        public String toString() {
            return "Task{" +
                    "type='" + type + '\'' +
                    '}';
        }

        /**
         * Worker Task Types
         */
        public enum Type {
            PROCESS_FEEDS
        }
    }
}
