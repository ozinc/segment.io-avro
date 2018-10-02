package com.oz.segmentio.segment;


import avro.shaded.com.google.common.collect.ImmutableMap;

import com.segment.analytics.Analytics;
import com.segment.analytics.Callback;
import com.segment.analytics.messages.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;


public final class JavaSegmentTest {

    private final String testApiKey = "R4GYcoNaPSg5oXFn7Vp0J6Y9q1YT6CWE";
    private final Logger logger = LoggerFactory.getLogger(JavaSegmentTest.class);

    private final Map glbContext = ImmutableMap.of("app",ImmutableMap.of("name","Segment"));

    @Test
    public void runTrackTests() {

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
                        logger.debug("Arrived!");
                    }

                    @Override public void failure(Message message, Throwable throwable) {
                        logger.error("Failed: " + message);
                    }
                }) //
                .messageInterceptor(message -> {
                    logger.debug("Message interceptor {}",message);
                    return message;
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
        //Create OZ Group messages // for each organization, community, service
        for (MessageBuilder message : buildGroupMessages()) analytics.enqueue(message);
        //Create OZ SaaS / User Messages
        for (MessageBuilder message : buildSaaSMessages()) analytics.enqueue(message);
        //Create OZ Video Messages
        for (MessageBuilder message : buildVideoSMessages()) analytics.enqueue(message);
        //Create OZ Video Messages
        for (MessageBuilder message : buildScreenPageMessages()) analytics.enqueue(message);

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

        messages.add(TrackMessage.builder("Order Completed").userId("f4ca124298").anonymousId("smeom").properties(orderProps).context(ImmutableMap.of("groupId","0909")));

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
                "source","www.mbl.is", //Campaign source — attributed ad network -- Network/FB/AdWords/MoPub/Source
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

        Map<String, Object> groupTraits = new HashMap<>();
        //Creating a group without user participation
        groupTraits.put("name","Some Community Name 2"); //The name of the account being created.
        groupTraits.put("type","Athlete"); //The name of the account being created.
        groupTraits.put("domain","Sport"); //The name of the account being created.
        groupTraits.put("category","Soccer"); //The name of the account being created.
        messages.add(GroupMessage.builder("Community/<id1>").userId("<studio-user>").traits(groupTraits).context(ImmutableMap.of("test_addition",true)));

        //User joins a community
        messages.add(GroupMessage.builder("Community/<id2>").userId("f4ca124298"));

        //User leaving a community
        //todo - not supported by Segment? -- messages.add(GroupMessage.builder("Community/<id>").userId("f4ca124298"));

        return messages;
    }

    private  Collection<MessageBuilder> buildScreenPageMessages() {
        Collection<MessageBuilder> messages = new ArrayList<>();

        Map<String, Object> pageProps = new HashMap<>();
        pageProps.put("title","Som title from web page"); //Title of the page
        pageProps.put("keywords", Arrays.asList("video","sports")); //A list/array of kewords describing the content of the page.  The keywords would most likely be the same as, or similar to, the keywords you would find in an html meta tag for SEO purposes. This property is mainly used by content publishers that rely heavily on pageview tracking. This is not automatically collected.
        pageProps.put("url","https://www.oz.com/stod2sport/live/8a4c2e36-fef5-41bf-b4fa-3c13be80f581"); //Full URL of the page. First we look for the canonical url. If the canonical url is not provided, we use location.href from the DOM API.
        //pageProps.put("search","soccer"); //Query string portion of the URL of the page
        pageProps.put("referrer","https://www.mbl.is/frettir/"); //Full URL of the previous page Equivalent to document.referrer from the DOM API.
        pageProps.put("path","/concacafnationsleage"); //Path portion of the URL of the page. Equivalent to canonical path which defaults to location.pathname from the DOM API.
        messages.add(PageMessage.builder("Stöð 2 Sport").userId("f4ca124298").properties(pageProps));

        //
        pageProps.put("url","https://www.oz.com/stod2sport/schedule"); //Full URL of the page. First we look for the canonical url. If the canonical url is not provided, we use location.href from the DOM API.
        pageProps.remove("referrer"); //Full URL of the page. First we look for the canonical url. If the canonical url is not provided, we use location.href from the DOM API.
        pageProps.remove("keywords"); //Full URL of the page. First we look for the canonical url. If the canonical url is not provided, we use location.href from the DOM API.
        messages.add(PageMessage.builder("Dagskrá - Stöð 2 Sport").userId("f4ca124298").properties(pageProps));
        messages.add(PageMessage.builder("Dagskrá - Stöð 2 Sport").userId("f4ca124298").properties(pageProps));
        return messages;
    }
    private  Collection<MessageBuilder> buildVideoSMessages() {
        Collection<MessageBuilder> messages = new ArrayList<>();

        Map<String, Object> videoProps = new HashMap<>();
        videoProps.put("session_id","12345");
        videoProps.put("content_asset_ids", Arrays.asList("segA"));
        videoProps.put("content_pod_ids", Arrays.asList("segA","segB"));
        videoProps.put("ad_asset_id", Arrays.asList("segA","segB"));
        videoProps.put("ad_type", Arrays.asList("mid-roll","post-roll"));
        videoProps.put("position", 0);
        videoProps.put("total_length", 500);
        videoProps.put("bitrate", 100);
        videoProps.put("framerate", 50);
        videoProps.put("video_player", "hls player");
        videoProps.put("sound", 88);
        videoProps.put("full_screen", false);
        videoProps.put("ad_enabled", true);
        videoProps.put("quality", "hls hd1080");
        videoProps.put("livestream", false);
        messages.add(TrackMessage.builder("Video Playback Started").userId("f4ca124298").properties(videoProps));
        messages.add(TrackMessage.builder("Video Playback Paused").userId("f4ca124298").properties(videoProps));
        messages.add(TrackMessage.builder("Video Playback Interrupted").userId("f4ca124298").properties(videoProps));
        messages.add(TrackMessage.builder("Video Playback Buffer Started").userId("f4ca124298").properties(videoProps));
        messages.add(TrackMessage.builder("Video Playback Buffer Completed").userId("f4ca124298").properties(videoProps));
        messages.add(TrackMessage.builder("Video Playback Seek Started").userId("f4ca124298").properties(videoProps));
        messages.add(TrackMessage.builder("Video Playback Seek Completed").userId("f4ca124298").properties(videoProps));
        messages.add(TrackMessage.builder("Video Playback Resumed").userId("f4ca124298").properties(videoProps));
        messages.add(TrackMessage.builder("Video Playback Completed").userId("f4ca124298").properties(videoProps));
        messages.add(TrackMessage.builder("Video Playback Completed").userId("f4ca124298").properties(videoProps));

        Map<String, Object> videoContentProps = new HashMap<>();
        videoContentProps.put("session_id","12345"); //User session, video session, browser session if available
        videoContentProps.put("asset_id", "segA"); // the stream, slot or vod id being played
        videoContentProps.put("pod_id", "segB"); // if this is part of a video plaback collection/playlist then which section is this
        videoContentProps.put("program", "Planet Earth"); //Name of the series / program / league
        videoContentProps.put("title", "Planet Seasonal Forests"); //Name of the episode, match, slot
        videoContentProps.put("description", "David Attenborough reveals the greatest woodlands on earth."); //Description
        videoContentProps.put("season", "1"); //What season is this
        videoContentProps.put("episode", "1"); //What episode is this
        videoContentProps.put("genre", "Documentary"); //Genre of the program. Use path for main, sub-categories Sports/Football
        videoContentProps.put("publisher", "BBC"); //Service or series producer
        videoContentProps.put("full_episode", true); //is this a full show or a part of a show
        videoContentProps.put("livestream", false);
        videoContentProps.put("keywords", Arrays.asList("nature","forests","earth")); //any keywords/tags to attach to the playback
        videoContentProps.put("position", 0); //Current position
        videoContentProps.put("total_length", 3983); //Total length
        messages.add(TrackMessage.builder("Video Content Started").userId("f4ca124298").properties(videoContentProps));
        videoContentProps.put("position", 1000);
        messages.add(TrackMessage.builder("Video Content Playing").userId("f4ca124298").properties(videoContentProps));
        videoContentProps.put("position", 2000);
        messages.add(TrackMessage.builder("Video Content Playing").userId("f4ca124298").properties(videoContentProps));
        videoContentProps.put("position", 3000);
        messages.add(TrackMessage.builder("Video Content Playing").userId("f4ca124298").properties(videoContentProps));
        videoContentProps.put("position", 3983);
        messages.add(TrackMessage.builder("Video Content Completed").userId("f4ca124298").properties(videoContentProps));


        Map<String, Object> videoAdProps = new HashMap<>();
        videoContentProps.put("session_id","12345"); //User session, video session, browser session if available
        videoContentProps.put("asset_id", "0129370"); //is this a full show or a part of a show
        videoContentProps.put("pod_id", "segA"); //is this a full show or a part of a show
        videoContentProps.put("type", "pre-roll"); //is this a full show or a part of a show
        videoContentProps.put("title", "The New New Thing!"); //is this a full show or a part of a show
        videoContentProps.put("position", 0); //is this a full show or a part of a show
        videoContentProps.put("total_length", 30); //is this a full show or a part of a show
        videoContentProps.put("publisher", "Apple"); //is this a full show or a part of a show
        videoContentProps.put("load_type", "dynamic"); //is this a full show or a part of a show
        messages.add(TrackMessage.builder("Video Ad Started").userId("f4ca124298").properties(videoAdProps));
        videoContentProps.put("position", 5); //is this a full show or a part of a show
        messages.add(TrackMessage.builder("Video Ad Playing").userId("f4ca124298").properties(videoAdProps));
        videoContentProps.put("position", 30); //is this a full show or a part of a show
        messages.add(TrackMessage.builder("Video Ad Completed").userId("f4ca124298").properties(videoAdProps));

        Map<String, Object> videoQualityProps = new HashMap<>();
        videoQualityProps.put("bitrate", 100);
        videoQualityProps.put("framerate", 50);
        videoQualityProps.put("startupTime", 50);
        videoQualityProps.put("droppedFrames", 10);
        messages.add(TrackMessage.builder("Video Quality Event").userId("f4ca124298").properties(videoQualityProps));


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


        Map<String, Object> authenticationProps = new HashMap<>();
        authenticationProps.put("username",signupProps.get("username"));
        messages.add(TrackMessage.builder("Signed In").userId("<studio-user>").properties(authenticationProps));
        messages.add(TrackMessage.builder("Signed Out").userId("<studio-user>").properties(authenticationProps));


        Map<String, Object> invitationProps = new HashMap<>();
        invitationProps.put("invitee_email","stebax@gmail.com"); //The email address of the person receiving the invite.
        invitationProps.put("invitee_first_name","Stefán"); //	The first name of the person receiving the invite.
        invitationProps.put("invitee_last_name","Baxter"); //The last name of the person receiving the invite.
        invitationProps.put("invitee_role","Promoter"); //The permission group for the person receiving the invite.
        messages.add(TrackMessage.builder("Invite Sent").userId("<studio-user>").properties(invitationProps));

        Map<String, Object> studioActionProps = new HashMap<>();
        studioActionProps.put("studio_user","<studio-user>"); //The one deleting the other
        messages.add(TrackMessage.builder("Account Removed User").userId("<studio-user-being-deleted>").properties(studioActionProps));

        Map<String, Object> trialProps = new HashMap<>();
        trialProps.put("trial_start_date","20180110T00:00:00.000T"); //The date when the trial starts. It is an ISO-8601 date string.
        trialProps.put("trial_end_date","20183110T00:00:00.000T"); //he date when the trial ends. It is an ISO-8601 date string.
        trialProps.put("trial_plan_name","Free Pass for Smu"); //The name of the plan being trialed.
        messages.add(TrackMessage.builder("Trial Started").userId("<user_id>").properties(trialProps));
        messages.add(TrackMessage.builder("Trial Ended").userId("<user_id>").properties(trialProps));

        messages.add(AliasMessage.builder("<user_id>").userId("<new-id>"));

        return messages;
    }

}
