package com.oz.segmentio.segment;

import com.segment.analytics.Analytics;
import com.segment.analytics.Callback;
import com.segment.analytics.Log;
import com.segment.analytics.Plugin;
import com.segment.analytics.messages.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingPlugin implements Plugin {

    private final Logger logger = LoggerFactory.getLogger(LoggingPlugin.class);

    @Override
    public void configure(Analytics.Builder builder) {
        builder.log(
                new Log() {
                    @Override
                    public void print(Level level, String format, Object... args) {
                        logger.debug(level + ":\t" + String.format(format, args));
                    }

                    @Override
                    public void print(Level level, Throwable error, String format, Object... args) {
                        logger.error(level + ":\t" + String.format(format, args),error);
                    }
                });

        builder.callback(
                new Callback() {
                    @Override
                    public void success(Message message) {
                        logger.warn("Uploaded " + message.toString());
                    }

                    @Override
                    public void failure(Message message, Throwable throwable) {
                        logger.error("Could not upload " + message, throwable);
                    }
                });
    }
}