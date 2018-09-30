package com.oz.segmentio.segment;


import avro.shaded.com.google.common.collect.ImmutableMap;

import com.segment.analytics.Analytics;
import com.segment.analytics.Callback;
import com.segment.analytics.MessageInterceptor;
import com.segment.analytics.messages.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;


public final class JavaSegmentTest {

    String testApiKey = "R4GYcoNaPSg5oXFn7Vp0J6Y9q1YT6CWE";
    Logger logger = LoggerFactory.getLogger(JavaSegmentTest.class);

    @Test
    public void runTrackTests() throws Exception {

        final BlockingFlush blockingFlush = BlockingFlush.create();

        Analytics analytics = Analytics
                .builder(testApiKey)
                .userAgent("smu")
                .flushQueueSize(20)
                .flushInterval(1, TimeUnit.SECONDS)
                .plugin(blockingFlush.plugin())
                .plugin(new LoggingPlugin())
                .callback(new Callback() {
                    @Override public void success(Message message) {
                        logger.warn("Arrived!");
                    }

                    @Override public void failure(Message message, Throwable throwable) {
                        logger.error("Failed: " + message);
                    }
                }) //
                .messageInterceptor(new MessageInterceptor() {
                    @Override public Message intercept(Message message) {
                        logger.debug("Message interceptor {}",message);
                        return message;
                    }
                })
                .build();

        analytics.enqueue(IdentifyMessage.builder()
                .userId("f4ca124298")
                .anonymousId("smeom")
                .traits(ImmutableMap.of("name", "Michael Bolton","email", "mbolton@initech.com"))
                .context(ImmutableMap.of("app",ImmutableMap.of("name","Segment")))
        );

        for (int i = 0; i<50; i++) {
            MessageBuilder track = TrackMessage
                    .builder("Clicked CTA")
                    .userId("f4ca124298")
                    .anonymousId("smeom")
                    .context(ImmutableMap.of("app",ImmutableMap.of("name","Segment")))
                    .properties(ImmutableMap.of("location", "header","type", "button"));

            analytics.enqueue(track);

            /*
            MessageBuilder message = TrackMessage.builder("Schedule")
                    .userId("f4ca124298")
                    .enableIntegration("AS",true)
                    .properties(ImmutableMap.of("category", "Sports","path", "/sports/schedule"))
                    .context(ImmutableMap.of())
                    .anonymousId("smeom");
            analytics.enqueue(message);
            */

        }
        analytics.flush();
        blockingFlush.block();
        analytics.shutdown();
    }
}
