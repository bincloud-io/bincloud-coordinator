package io.bce.logging;

import io.bce.text.TextProcessor;
import io.bce.text.TextTemplates;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This class is the application logger implementation based on the {@link Logger} logging
 * mechanism.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public class JulLogger extends AbstractLogger implements ApplicationLogger {
  private Logger logger;
  private TextProcessor messageProcessor;

  /**
   * Create logger for specified text processor.
   *
   * @param textProcessor The text processor
   */
  public JulLogger(TextProcessor textProcessor) {
    super();
    this.logger = Logger.getAnonymousLogger();
    this.messageProcessor = textProcessor;
  }

  private JulLogger(TextProcessor messageProcessor, String name) {
    super();
    this.messageProcessor = messageProcessor;

    this.logger = Logger.getLogger(name);
  }

  @Override
  public void log(LogRecord logRecord) {
    LogRecord transformedRecord = logRecord.transformMessage(template -> TextTemplates
        .createBy(messageProcessor.interpolate(template), template.getParameters()));
    this.logger.log(new JulRecord(transformedRecord));
  }

  @Override
  public ApplicationLogger named(String loggerName) {
    return new JulLogger(messageProcessor, loggerName);
  }

  private static class JulRecord extends java.util.logging.LogRecord {
    private static final long serialVersionUID = 196133523390302741L;

    public JulRecord(LogRecord logRecord) {
      super(new JulLevel(logRecord.getLevel()), logRecord.getMessageText());
      setMillis(logRecord.getTimestamp().toEpochMilli());
    }
  }

  private static class JulLevel extends java.util.logging.Level {
    private static final long serialVersionUID = -6609526190118526044L;
    private static final Map<Level, Integer> SEVERITY_FACTOR_MAP = createSeverityFactorMap();

    public JulLevel(Level level) {
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
