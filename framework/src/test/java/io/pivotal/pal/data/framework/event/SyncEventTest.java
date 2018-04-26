package io.pivotal.pal.data.framework.event;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SyncEventTest {

    private static final String EVENT_NAME = "test";

    @Test
    public void success_withoutRetry() {
        DefaultSyncEventPublisher<String, String> publisher = new DefaultSyncEventPublisher<>(EVENT_NAME);
        Handler handler = new Handler();
        SyncEventSubscriberAdapter<String, String> subscriber = new SyncEventSubscriberAdapter<>(EVENT_NAME, handler, null, 0, 0, 0);

        handler.setData(null);

        String someData = "some-data";
        String result = publisher.publish(someData);

        assertThat(handler.getData()).isEqualTo(someData);
        assertThat(result).isEqualTo(someData);
    }

    @Test
    public void success_withRetry() {
        DefaultSyncEventPublisher<String, String> publisher = new DefaultSyncEventPublisher<>(EVENT_NAME);
        ExceptionThrowingHandler handler = new ExceptionThrowingHandler(1);
        SyncEventSubscriberAdapter<String, String> subscriber = new SyncEventSubscriberAdapter<>(EVENT_NAME, handler, null, 1, 100, 2);

        handler.setData(null);

        String someData = "some-data";
        String result = publisher.publish(someData);

        assertThat(handler.getData()).isEqualTo(someData);
        assertThat(result).isEqualTo(someData);
    }

    @Test
    public void error_withHandlerNoRetry() {
        DefaultSyncEventPublisher<String, String> publisher = new DefaultSyncEventPublisher<>(EVENT_NAME);
        ExceptionThrowingHandler handler = new ExceptionThrowingHandler(1);
        Handler errorHandler = new Handler();
        SyncEventSubscriberAdapter<String, String> subscriber = new SyncEventSubscriberAdapter<>(EVENT_NAME, handler, errorHandler, 0, 0, 0);

        String someData = "some-data";
        String result = publisher.publish(someData);

        assertThat(errorHandler.getData()).isEqualTo(someData);
        assertThat(result).isEqualTo(someData);
    }

    @Test
    public void error_withoutHandlerNoRetry() {
        DefaultSyncEventPublisher<String, String> publisher = new DefaultSyncEventPublisher<>(EVENT_NAME);
        ExceptionThrowingHandler handler = new ExceptionThrowingHandler(1);
        SyncEventSubscriberAdapter<String, String> subscriber = new SyncEventSubscriberAdapter<>(EVENT_NAME, handler, null, 0, 0, 0);

        String someData = "some-data";
        String result = publisher.publish(someData);

        assertThat(result).isNull();
    }

    private class ExceptionThrowingHandler implements SyncEventHandler<String, String> {

        int maxCount;
        int count = 0;
        String data;

        public ExceptionThrowingHandler(int maxCount) {
            this.maxCount = maxCount;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        @Override
        public String onEvent(String data) {
            if (count++ < maxCount) {
                throw new IllegalArgumentException("data = " + data);
            } else {
                this.data = data;
                return data;
            }
        }
    }

    private class Handler implements SyncEventHandler<String, String> {

        private String data;

        public Handler() {
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        @Override
        public String onEvent(String data) {
            this.data = data;
            return data;
        }
    }
}