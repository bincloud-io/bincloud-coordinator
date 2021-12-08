package io.bce.logging;

import io.bce.domain.ErrorDescriptorTemplate;
import io.bce.domain.errors.ErrorDescriptor;
import io.bce.text.TextTemplate;
import io.bce.text.TextTemplate.Transformer;
import io.bce.text.TextTemplates;
import java.time.Instant;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;

/**
 * This class represents the log record.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@Getter
@ToString
public class LogRecord {
  private Level level;
  private Instant timestamp;
  @Getter(AccessLevel.NONE)
  private TextTemplate message;

  /**
   * Create the log record for specified object.
   *
   * @param level          The log level
   * @param loggableObject The loggable object
   */
  public LogRecord(Level level, Object loggableObject) {
    this(level, TextTemplates.createBy(loggableObject));
  }

  /**
   * Create the log record for specified text.
   *
   * @param level       The log level
   * @param messageText The loggable text
   */
  public LogRecord(Level level, String messageText) {
    this(level, TextTemplates.createBy(messageText));
  }

  /**
   * Create the log record for specified error descriptor.
   *
   * @param level           The log level
   * @param errorDescriptor The error descriptor
   */
  public LogRecord(Level level, ErrorDescriptor errorDescriptor) {
    this(level, ErrorDescriptorTemplate.createFor(errorDescriptor));
  }

  /**
   * Create the log record for specified message text template.
   *
   * @param level   The log level
   * @param message The message text template
   */
  public LogRecord(Level level, TextTemplate message) {
    super();
    this.level = level;
    this.message = message;
    this.timestamp = Instant.now();
  }

  private LogRecord(LogRecord proto) {
    super();
    this.level = proto.level;
    this.timestamp = proto.timestamp;
    this.message = proto.message;
  }

  /**
   * Transform the log message.
   *
   * @param transformer The log message transformer
   * @return The transformed log record
   */
  public LogRecord transformMessage(Transformer transformer) {
    LogRecord derived = new LogRecord(this);
    derived.message = transformer.transform(message);
    return derived;
  }

  /**
   * Get the message text.
   *
   * @return The message text string
   */
  public String getMessageText() {
    return message.getTemplateText();
  }

  /**
   * Get the message parameters.
   *
   * @return The log message parameters map.
   */
  public Map<String, Object> getMessageParameters() {
    return message.getParameters();
  }
}
