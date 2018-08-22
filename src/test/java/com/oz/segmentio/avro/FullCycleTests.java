package com.oz.segmentio.avro;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.avro.AvroMapper;
import com.fasterxml.jackson.dataformat.avro.AvroSchema;
import com.oz.segmentio.json.JsonToAvroUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.avro.Schema;
import org.apache.avro.specific.SpecificRecordBase;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import static org.junit.Assert.fail;

public final class FullCycleTests {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final AvroMapper AVRO_MAPPER = new AvroMapper();

    private static final List<Schema.Type> SCALAR_AVRO_TYPES = Arrays.asList(
        Schema.Type.NULL, Schema.Type.INT, Schema.Type.STRING, Schema.Type.LONG, Schema.Type.ENUM,
        Schema.Type.FLOAT, Schema.Type.DOUBLE, Schema.Type.BOOLEAN
    );

    private static final List<String> B2B_SAAS_TESTS = Arrays.asList(
        "AccountCreated", "AccountDeleted", "SignedUp", "LoggedIn", "InviteSent",
        "AccountAddedUser", "AccountRemovedUser", "TrialStarted", "TrialEnded"
    );

    private static final List<String> CAMPAIGN_RELATED_TESTS = Arrays.asList(
        "DeepLinkClicked", "DeepLinkOpened", "InstallAttributed", "PushNotificationBounced",
        "PushNotificationReceived", "PushNotificationTapped"
    );

    private static final List<String> LIFECYCLE_RELATED_TESTS = Arrays.asList(
        "ApplicationOpened", "ApplicationBackgrounded", "ApplicationCrashed",
        "ApplicationInstalled", "ApplicationUninstalled", "ApplicationUpdated"
    );

    @Test
    public void identifyFullCycle() throws IOException {
        assertFullCycle("Identify", Identify.getClassSchema(), Identify.class);
    }

    @Test
    public void pageFullCycle() throws IOException {
        assertFullCycle("Page", Page.getClassSchema(), Page.class);
    }

    @Test
    public void aliasFullCycle() throws IOException {
        assertFullCycle("Alias", Alias.getClassSchema(), Alias.class);
    }

    @Test
    public void screenFullCycle() throws IOException {
        assertFullCycle("Screen", Screen.getClassSchema(), Screen.class);
    }

    @Test
    public void trackFullCycle() throws IOException {
        assertFullCycle("Track", Track.getClassSchema(), Track.class);
    }

    @Test
    public void groupFullCycle() throws IOException {
        assertFullCycle("Group", Group.getClassSchema(), Group.class);
    }

    @Test
    public void productListViewedFullCycle() throws IOException {
        assertFullCycle(
            "ProductListViewed", ProductListViewed.getClassSchema(), ProductListViewed.class
        );
    }

    @Test
    public void productListFilteredFullCycle() throws IOException {
        assertFullCycle(
            "ProductListFiltered", ProductListFiltered.getClassSchema(), ProductListFiltered.class
        );
    }

    @Test
    public void productViewedFullCycle() throws IOException {
        assertFullCycle(
            "ProductViewed", ProductViewed.getClassSchema(), ProductViewed.class
        );
    }

    @Test
    public void productClickedFullCycle() throws IOException {
        assertFullCycle(
            "ProductClicked", ProductClicked.getClassSchema(), ProductClicked.class
        );
    }

    @Test
    public void productAddedFullCycle() throws IOException {
        assertFullCycle(
            "ProductAdded", ProductAdded.getClassSchema(), ProductAdded.class
        );
    }

    @Test
    public void productRemovedFullCycle() throws IOException {
        assertFullCycle(
            "ProductRemoved", ProductRemoved.getClassSchema(), ProductRemoved.class
        );
    }

    @Test
    public void cartViewedFullCycle() throws IOException {
        assertFullCycle("CartViewed", CartViewed.getClassSchema(), CartViewed.class);
    }

    @Test
    public void checkoutStartedFullCycle() throws IOException {
        assertFullCycle("CheckoutStarted", CheckoutStarted.getClassSchema(), CheckoutStarted.class);
    }

    @Test
    public void checkoutStepViewedFullCycle() throws IOException {
        assertFullCycle(
            "CheckoutStepViewed", CheckoutStepViewed.getClassSchema(), CheckoutStepViewed.class
        );
    }

    @Test
    public void checkoutStepCompletedFullCycle() throws IOException {
        assertFullCycle(
            "CheckoutStepCompleted", CheckoutStepCompleted.getClassSchema(), CheckoutStepCompleted.class
        );
    }

    @Test
    public void paymentInfoEnteredFullCycle() throws IOException {
        assertFullCycle(
            "PaymentInfoEntered", PaymentInfoEntered.getClassSchema(), PaymentInfoEntered.class
        );
    }

    @Test
    public void orderUpdatedFullCycle() throws IOException {
        assertFullCycle(
            "OrderUpdated", OrderUpdated.getClassSchema(), OrderUpdated.class
        );
    }

    @Test
    public void orderCompletedFullCycle() throws IOException {
        assertFullCycle(
            "OrderCompleted", OrderCompleted.getClassSchema(), OrderCompleted.class
        );
    }

    @Test
    public void orderRefundedFullCycle() throws IOException {
        assertFullCycle(
            "OrderRefunded", OrderRefunded.getClassSchema(), OrderRefunded.class
        );
    }

    @Test
    public void orderCancelledFullCycle() throws IOException {
        assertFullCycle(
            "OrderCancelled", OrderCancelled.getClassSchema(), OrderCancelled.class
        );
    }

    @Test
    public void promotionClickedFullCycle() throws IOException {
        assertFullCycle(
            "PromotionClicked", PromotionClicked.getClassSchema(), PromotionClicked.class
        );
    }

    @Test
    public void promotionViewedFullCycle() throws IOException {
        assertFullCycle(
            "PromotionViewed", PromotionViewed.getClassSchema(), PromotionViewed.class
        );
    }

    @Test
    public void productsSearchedFullCycle() throws IOException {
        assertFullCycle(
            "ProductsSearched", ProductsSearched.getClassSchema(), ProductsSearched.class
        );
    }

    @Test
    public void couponEnteredFullCycle() throws IOException {
        assertFullCycle(
            "CouponEntered", CouponEntered.getClassSchema(), CouponEntered.class
        );
    }

    @Test
    public void couponAppliedFullCycle() throws IOException {
        assertFullCycle(
            "CouponApplied", CouponApplied.getClassSchema(), CouponApplied.class
        );
    }

    @Test
    public void couponDeniedFullCycle() throws IOException {
        assertFullCycle(
            "CouponDenied", CouponDenied.getClassSchema(), CouponDenied.class
        );
    }

    @Test
    public void couponRemovedFullCycle() throws IOException {
        assertFullCycle(
            "CouponRemoved", CouponRemoved.getClassSchema(), CouponRemoved.class
        );
    }

    @Test
    public void productAddedToWishlistFullCycle() throws IOException {
        assertFullCycle(
            "ProductAddedToWishlist", ProductAddedToWishlist.getClassSchema(), ProductAddedToWishlist.class
        );
    }

    @Test
    public void productRemovedFromWishlistFullCycle() throws IOException {
        assertFullCycle(
            "ProductRemovedFromWishlist", ProductRemovedFromWishlist.getClassSchema(), ProductRemovedFromWishlist.class
        );
    }

    @Test
    public void wishlistProductAddedToCartFullCycle() throws IOException {
        assertFullCycle(
            "WishlistProductAddedToCart", WishlistProductAddedToCart.getClassSchema(), WishlistProductAddedToCart.class
        );
    }

    @Test
    public void productSharedFullCycle() throws IOException {
        assertFullCycle("ProductShared", ProductShared.getClassSchema(), ProductShared.class);
    }

    @Test
    public void cartSharedFullCycle() throws IOException {
        assertFullCycle("CartShared", CartShared.getClassSchema(), CartShared.class);
    }

    @Test
    public void productReviewedFullCycle() throws IOException {
        assertFullCycle("ProductReviewed", ProductReviewed.getClassSchema(), ProductReviewed.class);
    }

    @Test
    public void lifeCycleEventFullCycle() throws IOException {
        for (String testCase : LIFECYCLE_RELATED_TESTS) {
            assertFullCycle(testCase, LifeCycleEvent.getClassSchema(), LifeCycleEvent.class);
        }
    }

    @Test
    public void campaignRelatedEventFullCycle() throws IOException {
        for (String testCase : CAMPAIGN_RELATED_TESTS) {
            assertFullCycle(testCase, CampaignRelatedEvent.getClassSchema(), CampaignRelatedEvent.class);
        }
    }

    @Test
    public void videoPlaybackStartedFullCycle() throws IOException {
        assertFullCycle("VideoPlaybackStarted", PlaybackEvent.getClassSchema(), PlaybackEvent.class);
    }

    @Test
    public void videoContentStartedFullCycle() throws IOException {
        assertFullCycle("VideoContentStarted", ContentEvent.getClassSchema(), ContentEvent.class);
    }

    @Test
    public void videoAdStartedFullCycle() throws IOException {
        assertFullCycle("VideoAdStarted", AdEvent.getClassSchema(), AdEvent.class);
    }

    @Test
    public void b2BSaaSFullCycle() throws IOException {
        for (String testCase : B2B_SAAS_TESTS) {
            assertFullCycle(testCase, B2BSaaS.getClassSchema(), B2BSaaS.class);
        }
    }

    @Test
    public void trackingFullCycle() throws IOException {
        for (String testCase : B2B_SAAS_TESTS) {
            assertFullCycle(testCase, Tracking.getClassSchema(), Tracking.class);
        }
        for (String testCase : CAMPAIGN_RELATED_TESTS) {
            assertFullCycle(testCase, Tracking.getClassSchema(), Tracking.class);
        }
        for (String testCase : LIFECYCLE_RELATED_TESTS) {
            assertFullCycle(testCase, Tracking.getClassSchema(), Tracking.class);
        }
    }

    private static void assertFullCycle(String name, Schema avro, Class<?> clazz) throws IOException {
        try (InputStream input = FullCycleTests.class.getResourceAsStream(name + ".json")) {
            @SuppressWarnings("unchecked") final Map<String, Object> data = JsonToAvroUtils.sanitizeNumericTypes(
                avro,
                JsonToAvroUtils.sanitizeJsonKeysDeep(OBJECT_MAPPER.readValue(input, Map.class))
            );
            AvroSchema schema = new AvroSchema(avro);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            AVRO_MAPPER.writer(schema).writeValue(baos, data);
            assertAvroEqualsMap(
                AVRO_MAPPER.reader(schema).forType(clazz).readValue(baos.toByteArray()), data
            );
        }
    }

    @SuppressWarnings("unchecked")
    private static void assertAvroEqualsMap(SpecificRecordBase avro, Map<String, Object> map) {
        if (avro == null) {
            Assertions.assertThat(map).isNull();
            return;
        }
        avro.getSchema().getFields().forEach(
            field -> {
                Schema.Type fieldType = field.schema().getType();
                int fieldPos = field.pos();
                String fieldName = field.name();
                if (SCALAR_AVRO_TYPES.contains(fieldType) || fieldType == Schema.Type.MAP) {
                    Assertions.assertThat(avro.get(fieldPos)).isEqualTo(map.get(fieldName));
                } else if (fieldType == Schema.Type.RECORD) {
                    assertAvroEqualsMap(
                        (SpecificRecordBase) avro.get(fieldPos), (Map<String, Object>) map.get(fieldName)
                    );

                } else if (fieldType == Schema.Type.UNION) {
                    List<Schema.Type> subtypes = field.schema().getTypes().stream().filter(
                        s -> s.getType() != Schema.Type.NULL
                    ).map(Schema::getType).collect(Collectors.toList());
                    if (subtypes.size() != 1) {
                        fail("Expected exactly one type for field");
                    }
                    if (subtypes.get(0) == Schema.Type.RECORD) {
                        assertAvroEqualsMap((SpecificRecordBase) avro.get(fieldPos), (Map<String, Object>) map.get(fieldName));
                    } else {
                        Assertions.assertThat(avro.get(fieldPos)).isEqualTo(map.get(fieldName));
                    }
                } else if (fieldType == Schema.Type.ARRAY) {
                    deepAssertRecordsEqual(avro.get(fieldPos), map.get(fieldName));
                } else {
                    fail("[" + field + "]'s type is not implemented by this test framework yet.");
                }
            }
        );
    }

    @SuppressWarnings("unchecked")
    private static void deepAssertRecordsEqual(Object avro, Object json) {
        if (avro instanceof List) {
            List<Object> avroList = (List<Object>) avro;
            List<Object> jsonList = (List<Object>) json;
            int length = avroList.size();
            Assertions.assertThat(length).isEqualTo(jsonList.size());
            for (int i = 0; i < length; ++i) {
                deepAssertRecordsEqual(avroList.get(i), jsonList.get(i));
            }
        } else if (avro instanceof SpecificRecordBase) {
            assertAvroEqualsMap((SpecificRecordBase) avro, (Map<String, Object>) json);
        } else if (avro instanceof String || avro instanceof Enum) {
            Assertions.assertThat(avro).isEqualTo(json);
        } else {
            fail("[" + avro.getClass() + "]'s type is not implemented by this test framework yet.");
        }
    }
}
