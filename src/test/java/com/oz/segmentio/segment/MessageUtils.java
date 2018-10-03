package com.oz.segmentio.segment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.oz.segmentio.avro.TestUtil;
import com.segment.analytics.messages.AliasMessage;
import com.segment.analytics.messages.GroupMessage;
import com.segment.analytics.messages.IdentifyMessage;
import com.segment.analytics.messages.Message;
import com.segment.analytics.messages.TrackMessage;
import java.util.HashMap;
import java.util.Map;

public final class MessageUtils {

    private MessageUtils() {
        // Utility class
    }

    public static byte[] messageToJSON(Message message) throws JsonProcessingException {
        Map<String, Object> data = new HashMap<>();
        data.put("context", message.context());
        data.put("anonymousId", message.anonymousId());
        data.put("userId", message.userId());
        data.put("integrations", message.integrations());
        data.put("messageId", message.messageId());
        data.put("timestamp", message.timestamp());
        data.put("type", message.type().name());
        if (message instanceof IdentifyMessage) {
            data.put("traits", ((IdentifyMessage) message).traits());
        } else if (message instanceof TrackMessage) {
            data.put("properties", ((TrackMessage) message).properties());
            data.put("event", ((TrackMessage) message).event());
        } else if (message instanceof GroupMessage) {
            data.put("traits", ((GroupMessage) message).traits());
            data.put("groupId", ((GroupMessage) message).groupId());
        } else if (message instanceof AliasMessage) {
            data.put("previousId", ((AliasMessage) message).previousId());
        }
        return TestUtil.OBJECT_MAPPER.writerFor(Map.class).writeValueAsBytes(data);
    }
}
