package com.uefa.platform.service.b2bpush.core.domain.feed.listener;

import com.uefa.platform.service.b2bpush.core.domain.feed.service.FeedProcessorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class SQSWorkerTaskListenerTest {

    private SQSWorkerTaskListener listener;

    @Mock
    private FeedProcessorService feedProcessorService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.afterPropertiesSet();
        listener = new SQSWorkerTaskListener(taskExecutor, feedProcessorService);
    }

    @Test
    public void testReceiveTask() {
        listener = spy(listener);
        SQSWorkerTaskListener.Task task = new SQSWorkerTaskListener.Task(SQSWorkerTaskListener.Task.Type.PROCESS_FEEDS);
        doNothing().when(feedProcessorService).processStaticFeeds();
        listener.receive(task);
        verify(listener, timeout(500)).importData(eq(task));
    }
}
