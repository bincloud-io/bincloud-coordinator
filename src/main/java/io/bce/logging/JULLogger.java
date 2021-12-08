package io.bce.logging;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import io.bce.text.TextProcessor;
import io.bce.text.TextTemplates;

public class JULLogger extends AbstractLogger implements ApplicationLogger {
    private Logger logger;
    private TextProcessor messageProcessor;

    public JULLogger(TextProcessor messageProcessor) {
        super();
        this.logger = Logger.getAnonymousLogger();
        this.messageProcessor = messageProcessor;
    }

    private JULLogger(TextProcessor messageProcessor, String name) {
        super();
        this.messageProcessor = messageProcessor;

        this.logger = Logger.getLogger(name);
    }

    @Override
    public void log(LogRecord logRecord) {
        LogRecord transformedRecord = logRecord.transformMessage(
                template -> TextTemplates.createBy(messageProcessor.interpolate(template), template.getParameters()));
        this.logger.log(new JULRecord(transformedRecord));
    }

    @Override
    public ApplicationLogger named(String loggerName) {
        return new JULLogger(messageProcessor, loggerName);
    }

    private static class JULRecord extends java.util.logging.LogRecord {
        private static final long serialVersionUID = 196133523390302741L;

        public JULRecord(LogRecord logRecord) {
            super(new JULLevel(logRecord.getLevel()), logRecord.getMessageText());
            setMillis(logRecord.getTimestamp().toEpochMilli());
        }
    }

    private static class JULLevel extends java.util.logging.Level {
        private static final long serialVersionUID = -6609526190118526044L;
        private static final Map<Level, Integer> SEVERITY_FACTOR_MAP = createSeverityFactorMap();

        public JULLevel(Level level) {
            super(level.name(), SEVERITY_FACTOR_MAP.getOrDefault(level, INFO.intValue()));
        }
    }

    private static Map<Level, Integer> createSeverityFactorMap() {
        Map<Level, Integer> result = new HashMap<>();
        result.put(Level.CRITIC, java.util.logging.Level.SEVERE.intValue());
        result.put(Level.ERROR, java.util.logging.Level.SEVERE.intValue());
        result.put(Level.WARN, java.util.logging.Level.WARNING.intValue());
        result.put(Level.INFO, java.util.logging.Level.INFO.intValue());
        result.put(Level.DEBUG, java.util.logging.Level.FINE.intValue());
        result.put(Level.TRACE, java.util.logging.Level.FINEST.intValue());
        return result;
    }

}
