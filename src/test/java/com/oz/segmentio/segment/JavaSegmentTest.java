package com.oz.segmentio.segment;


import avro.shaded.com.google.common.collect.ImmutableMap;

import com.segment.analytics.Analytics;
import com.segment.analytics.Callback;
import com.segment.analytics.MessageInterceptor;
import com.segment.analytics.messages.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
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

        Map<String, Object> props = new HashMap<>();
        props.putAll(ImmutableMap.of(
                "product_id", "507f1f77bcf86cd799439011",
                "sku", "G-32",
                "category","Games",
                "name","Monopoly: 3rd Edition",
                "brand", "'Hasbro"));
        props.putAll(ImmutableMap.of(
                "variant","200 pieces",
                "price", 18.99,
                "quantity", 1,
                "coupon","MAYDEALS",
                "position", 3));
        props.putAll(ImmutableMap.of(
                "revenue",1,
                "currency","USD",
                "value",1.3,
                "url","https://www.example.com/product/path'",
                "image_url","https://www.example.com/product/path.jpg"));

        MessageBuilder purchase = TrackMessage
                .builder("Product Clicked")
                .userId("f4ca124298")
                .anonymousId("smeom")
                .context(ImmutableMap.of("app",ImmutableMap.of("name","Segment")))
                .properties(props);

        analytics.enqueue(purchase);

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

        analytics.flush();
        blockingFlush.block();
        analytics.shutdown();
    }
}
