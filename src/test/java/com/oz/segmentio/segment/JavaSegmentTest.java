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
                .flushQueueSize(100)
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


        //Create OZ Application Messages
        for (MessageBuilder message : applicationMessages()) analytics.enqueue(message);
        //Create OZ Product Messages
        for (MessageBuilder message : buildProductMessages()) analytics.enqueue(message);
        //Create OZ Group messages // for each community
        for (MessageBuilder message : buildGroupMessages()) analytics.enqueue(message);
        //Create OZ SaaS Messages
        for (MessageBuilder message : buildSaaSMessages()) analytics.enqueue(message);
        //Create OZ SaaS Messages
        for (MessageBuilder message : buildSaaSMessages()) analytics.enqueue(message);

        analytics.flush();
        blockingFlush.block();
        analytics.shutdown();
    }

    private Collection<MessageBuilder> buildProductMessages() {

        ArrayList<MessageBuilder> messages = new ArrayList<>();

        //Product 1 - Clicked and added to basket
        Map<String, Object> product1Props = new HashMap<>();
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

        messages.add(TrackMessage.builder("Product Clicked").userId("f4ca124298").anonymousId("smeom").properties(product1Props));
        messages.add(TrackMessage.builder("Product Viewed").userId("f4ca124298").anonymousId("smeom").properties(product1Props));
        product1Props.put("cart_id", "skdjsidjsdkdj29j");
        messages.add(TrackMessage.builder("Product Added").userId("f4ca124298").anonymousId("smeom").properties(product1Props));
        messages.add(TrackMessage.builder("Product Added").userId("f4ca124298").anonymousId("smeom").properties(product1Props));
        messages.add(TrackMessage.builder("Product Removed").userId("f4ca124298").anonymousId("smeom").properties(product1Props));
        product1Props.remove("cart_id");

        //Product 2 - Clicked and added to basket
        Map<String, Object> product2Props = new HashMap<>();
        product2Props.put("product_id","82738383");     //Service_id or bundle_id or Channel_id or Slot_id (Use the lowest ID you have)
        product2Props.put("name","UEFA Championship League Pass for 2018-109");     //Full slug name
        product2Props.put("brand","UEFA");              //Brand = "<Service>/<Channel>" only use "<Service>" if product_id = Channel_id (One level above the level used as product_id
        product2Props.put("category","League Pass");    //League Pass, Event Pass, Service Subscription, Organization Subscription
        product2Props.put("variant","Full");            //Full, Free Trial
        product2Props.put("quantity",1);                //Quantity bought
        product2Props.put("price",50);                  //price of individual units
        product2Props.put("currency","USD");            //Any other hypothetical monetary value (Like the full PPV price of a match)
        product2Props.put("value",50);                  //Total value of this line (quantity * price)

        messages.add(TrackMessage.builder("Product Clicked").userId("f4ca124298").anonymousId("smeom").properties(product2Props));

        Map<String, Object> cartProps = new HashMap<>();
        cartProps.put("products", Arrays.asList(product1Props, product2Props)); //List of sold products (See above)
        cartProps.put("cart_id", "skdjsidjsdkdj29j");
        messages.add(TrackMessage.builder("Cart Viewed").userId("f4ca124298").anonymousId("smeom").properties(cartProps));

        //Order completed
        Map<String, Object> orderProps = new HashMap<>();
        orderProps.put("order_id","O82738383");
        orderProps.put("affiliation","web");//Where was this sold
        orderProps.put("total",54);         //total after cost
        orderProps.put("shipping",2);       //what is charged for shipping
        orderProps.put("tax",2);            //how high is the tax added to revenue
        orderProps.put("discount",2);       //how much is the discount given much of revenue
        orderProps.put("revenue",50);       //What is the amount sold for -cost
        orderProps.put("coupon","N/A");     //sold
        orderProps.put("currency","USD");   //In what currency is the sale
        orderProps.put("cart_id", "skdjsidjsdkdj29j");
        orderProps.put("products", Arrays.asList(product1Props, product2Props)); //List of sold products (See above)

        messages.add(TrackMessage.builder("Checkout Started").userId("f4ca124298").anonymousId("smeom").properties(orderProps));

        Map<String, Object> checkoutProps = new HashMap<>();
        checkoutProps.put("checkout_id","50314b8e9bcf000000000000"); //ID from the checkout provider
        checkoutProps.put("order_id","O82738383");
        checkoutProps.put("step",1);                    //What step are you on?
        messages.add(TrackMessage.builder("Checkout Step Viewed").userId("f4ca124298").anonymousId("smeom").properties(checkoutProps));
        messages.add(TrackMessage.builder("Checkout Step Completed").userId("f4ca124298").anonymousId("smeom").properties(checkoutProps));

        checkoutProps.put("shipping_method", "Fedex");  //Shipping method selected (Not applicable for us)
        checkoutProps.put("payment_method", "Visa");    //What payment method is selected
        messages.add(TrackMessage.builder("Payment Info Entered").userId("f4ca124298").anonymousId("smeom").properties(checkoutProps));

        checkoutProps.put("step",2);
        messages.add(TrackMessage.builder("Checkout Step Viewed").userId("f4ca124298").anonymousId("smeom").properties(checkoutProps));
        messages.add(TrackMessage.builder("Checkout Step Completed").userId("f4ca124298").anonymousId("smeom").properties(checkoutProps));

        orderProps.put("context.groupId","0909");
        messages.add(TrackMessage.builder("Order Completed").userId("f4ca124298").anonymousId("smeom").properties(orderProps));

        return messages;
    }

    private Collection<MessageBuilder> applicationMessages() {
        Collection<MessageBuilder> messages = new ArrayList<>();

        Map<String, Object> applicationProps = new HashMap<>();
        applicationProps.put("version","1.0.1"); //The version installed.
        applicationProps.put("build",901); //The build number of the installed app..
        messages.add(TrackMessage.builder("Application Installed").userId("f4ca124298").properties(applicationProps));

        applicationProps.put("from_background",true); //The build number of the installed app..
        applicationProps.put("url","http://www.mbl.is"); //The build number of the installed app..
        applicationProps.put("referring_application","Concacaf Client"); //The build number of the installed app..
        messages.add(TrackMessage.builder("Application Opened").userId("f4ca124298").properties(applicationProps));
        messages.add(TrackMessage.builder("Application Backgrounded").userId("f4ca124298").properties(applicationProps));

        Map<String, Object> upgradeProps = new HashMap<>();
        applicationProps.put("previous_version","1.0.1"); //The previously recorded version.
        applicationProps.put("previous_build",901); //The previously recorded build.
        applicationProps.put("version","1.0.1"); //The version installed.
        applicationProps.put("build",1001); //The build number of the installed app..
        messages.add(TrackMessage.builder("Application Updated").userId("f4ca124298").properties(upgradeProps));


        messages.add(TrackMessage.builder("Application Uninstalled").userId("f4ca124298").properties(ImmutableMap.of()));
        messages.add(TrackMessage.builder("Application Crashed").userId("f4ca124298").properties(ImmutableMap.of()));

        Map<String, Object> attributionProps = new HashMap<>();
        applicationProps.put("provider","Tune/Kochava/Branch"); //The attribution provider.
        applicationProps.put("campaign",ImmutableMap.of(
                "source","Network/FB/AdWords/MoPub/Source", //Campaign source — attributed ad network
                "name","Campaign Name", //The name of the attributed campaign.
                // "content","Organic Content Title", //The content of the campaign.
                "medium","Web", //Identifies what type of link was used.
                "ad_creative","Red Hello World Ad", //The ad creative name.
                "ad_group","Some AD-Group ID" //The ad group name.
                )); //The attribution provider.
        messages.add(TrackMessage.builder("Install Attributed").userId("f4ca124298").properties(attributionProps));


        Map<String, Object> pushProps = new HashMap<>();
        pushProps.put("action","Accept"); //If this notification is “actionable“, the custom action tapped. Default: “Open”
        pushProps.put("campaign",ImmutableMap.of(
                "medium","Push", //Identifies what type of link was used (Push Notification).
                "source","Vendor Name", //Designates the push provider. (Optional)
                "name","Referral Flow", //Campaign Name.
                "content","Your friend invited you to play a match." //Push notification content content
        )); //The attribution provider.
        messages.add(TrackMessage.builder("Push Notification Received").userId("f4ca124298").properties(pushProps));
        messages.add(TrackMessage.builder("Push Notification Tapped").userId("f4ca124298").properties(pushProps));
        messages.add(TrackMessage.builder("Push Notification Bounced").userId("f4ca124298").properties(pushProps));
        messages.add(TrackMessage.builder("Deep Link Clicked").userId("f4ca124298").properties(pushProps));
        messages.add(TrackMessage.builder("Deep Link Opened").userId("f4ca124298").properties(pushProps));

        return messages;
    }

    private Collection<MessageBuilder> buildGroupMessages() {
        Collection<MessageBuilder> messages = new ArrayList<>();
        return messages;
    }

    private  Collection<MessageBuilder> buildSaaSMessages() {
        Collection<MessageBuilder> messages = new ArrayList<>();

        Map<String, Object> signupProps = new HashMap<>();
        signupProps.put("account_name","JohnnyBGood"); //The name of the account being created.
        messages.add(TrackMessage.builder("Account Created").userId("system").properties(signupProps));
        messages.add(TrackMessage.builder("Account Deleted").userId("<studio-user>").properties(signupProps));
        messages.add(TrackMessage.builder("Account Created").userId("system").properties(signupProps));


        signupProps.put("type","organic"); //The type of signup, e.g. invited, organic.
        signupProps.put("first_name","Jonathan"); //The first name of the user.
        signupProps.put("last_name","Goddrd");  //The last name of the user.
        signupProps.put("email","hotmale@hotmail.com"); //The email of the user.
        signupProps.put("phone","+354-8200000"); //The phone number of the user.
        signupProps.put("username","JohnnyBGood"); // //The username of the user.
        signupProps.put("title","OZ"); // The id of the account the user is joining.
        messages.add(TrackMessage.builder("Signed Up").userId("system").properties(signupProps));


        Map<String, Object> authenticationPropes = new HashMap<>();
        authenticationPropes.put("username",signupProps.get("username"));
        messages.add(TrackMessage.builder("Signed In").userId("<studio-user>").properties(signupProps));
        messages.add(TrackMessage.builder("Signed Out").userId("<studio-user>").properties(signupProps));


        Map<String, Object> invitationPropes = new HashMap<>();
        authenticationPropes.put("invitee_email","stebax@gmail.com"); //The email address of the person receiving the invite.
        authenticationPropes.put("invitee_first_name","Stefán"); //	The first name of the person receiving the invite.
        authenticationPropes.put("invitee_last_name","Baxter"); //The last name of the person receiving the invite.
        authenticationPropes.put("invitee_role","Promoter"); //The permission group for the person receiving the invite.
        messages.add(TrackMessage.builder("Invite Sent").userId("<studio-user>").properties(invitationPropes));

        Map<String, Object> studiActionMap = new HashMap<>();
        studiActionMap.put("studio_user","<studio-user>"); //The one deleting the other
        messages.add(TrackMessage.builder("Account Removed User").userId("<studio-user-being-deleted>").properties(studiActionMap));

        Map<String, Object> trialProps = new HashMap<>();
        trialProps.put("trial_start_date","20180110T00:00:00.000T"); //The date when the trial starts. It is an ISO-8601 date string.
        trialProps.put("trial_end_date","20183110T00:00:00.000T"); //he date when the trial ends. It is an ISO-8601 date string.
        trialProps.put("trial_plan_name","Free Pass for Smu"); //The name of the plan being trialed.
        messages.add(TrackMessage.builder("Trial Started").userId("<user_id>").properties(trialProps));
        messages.add(TrackMessage.builder("Trial Ended").userId("<user_id>").properties(trialProps));


        return messages;
    }

}
