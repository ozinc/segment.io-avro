package com.oz.segmentio.segment;


import avro.shaded.com.google.common.collect.ImmutableMap;

import com.segment.analytics.Analytics;
import com.segment.analytics.Callback;
import com.segment.analytics.MessageInterceptor;
import com.segment.analytics.messages.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;


public final class JavaSegmentTest {

    String testApiKey = "R4GYcoNaPSg5oXFn7Vp0J6Y9q1YT6CWE";
    Logger logger = LoggerFactory.getLogger(JavaSegmentTest.class);

    private final Map glbContext = ImmutableMap.of("app",ImmutableMap.of("name","Segment"));

    @Test
    public void runTrackTests() throws Exception {

        final BlockingFlush blockingFlush = BlockingFlush.create();

        Analytics analytics = Analytics
                .builder(testApiKey)
                .userAgent("Server")
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
                .context(glbContext)
        );

        //Create OZ User Messages
        for (MessageBuilder message : buildUserMessages()) {
            analytics.enqueue(message);
        }

        //Create OZ Product Messages
        for (MessageBuilder message : buildProductMessages()) {
            analytics.enqueue(message);
        }

        analytics.flush();
        blockingFlush.block();
        analytics.shutdown();
    }

    public Collection<MessageBuilder> buildProductMessages() {

        ArrayList<MessageBuilder> messages = new ArrayList<>();

        Map<String, Object> product1Props = new HashMap();
        product1Props.put("product_id","82738387"); //Service_id or bundle_id or Channel_id or Slot_id (Use the lowest ID you have)
        product1Props.put("name","Liverpool vs. Manchester Unites (CL 2018)");     //Full slug name
        product1Props.put("brand","UEFA Champions League");    //Brand = "<Service>/<Channel>" only use "<Service>" if product_id = Channel_id (One level above the level used as product_id
        product1Props.put("category","Event Pass"); //League Pass, Event Pass, Service Subscription, Organization Subscription
        product1Props.put("variant","Free Trial");  //Full, Free Trial
        product1Props.put("coupon","SKJW96KJSB");   //Discount or access coupon used to get access
        product1Props.put("quantity",1);  //Quantity bought
        product1Props.put("price",0);     //price of individual units
        product1Props.put("currency","USD");     //Any other hypothetical monetary value (Like the full PPV price of a match)
        product1Props.put("value",0);     //Total value of this line (quantity * price)

        messages.add(TrackMessage.builder("Product Clicked")
                .userId("f4ca124298")
                .anonymousId("smeom")
                .properties(product1Props)
        );

        Map<String, Object> product2Props = new HashMap();
        product2Props.put("product_id","82738383"); //Service_id or bundle_id or Channel_id or Slot_id (Use the lowest ID you have)
        product2Props.put("name","UEFA Championship League Pass for 2018-109");     //Full slug name
        product2Props.put("brand","UEFA");    //Brand = "<Service>/<Channel>" only use "<Service>" if product_id = Channel_id (One level above the level used as product_id
        product2Props.put("category","League Pass"); //League Pass, Event Pass, Service Subscription, Organization Subscription
        product2Props.put("variant","Full");  //Full, Free Trial
        product2Props.put("quantity",1);  //Quantity bought
        product2Props.put("price",50);     //price of individual units
        product2Props.put("currency","USD");     //Any other hypothetical monetary value (Like the full PPV price of a match)
        product2Props.put("value",50);     //Total value of this line (quantity * price)

        messages.add(TrackMessage.builder("Product Clicked")
                .userId("f4ca124298")
                .anonymousId("smeom")
                .properties(product2Props)
        );

        Map<String, Object> orderProps = new HashMap();
        orderProps.put("order_id","O82738383");
        orderProps.put("affiliation","web");//Where was this sold
        orderProps.put("total",54);       //total after cost
        orderProps.put("shipping",2);     //what is charged for shipping
        orderProps.put("tax",2);          //how high is the tax added to revenue
        orderProps.put("discount",2);     //how much is the discount given much of revenue
        orderProps.put("revenue",50);     //What is the amount sold for -cost
        orderProps.put("coupon","N/A");   //sold
        orderProps.put("currency","USD"); //In what currency is the sale
        orderProps.put("products", Arrays.asList(product1Props, product2Props)); //List of sold products (See above)

        messages.add(TrackMessage.builder("Order Completed")
                .userId("f4ca124298")
                .anonymousId("smeom")
                .properties(orderProps)
        );

        return messages;

    }

    public Collection<MessageBuilder> buildUserMessages() {

        Collection<MessageBuilder> messages = new ArrayList<>();


        return messages;

    }

}
