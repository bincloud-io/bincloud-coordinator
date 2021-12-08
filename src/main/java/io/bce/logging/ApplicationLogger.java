package io.bce.logging;

import io.bce.text.TextTemplate;

/**
 * This class declares the cross-framework contract for logging inside the application.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface ApplicationLogger {
  /**
   * Write the log record to the log.
   *
   * @param logRecord The log record
   */
  public void log(LogRecord logRecord);

  /**
   * Log text message with {@link Level#TRACE} level.
   *
   * @param message The message
   */
  public void trace(String message);

  /**
   * Log error stacktrace with {@link Level#TRACE} level.
   *
   * @param error The error
   */
  public void trace(Throwable error);

  /**
   * Log template message with {@link Level#TRACE} level.
   *
   * @param messageTemplate The message template
   */
  public void trace(TextTemplate messageTemplate);

  /**
   * Log text message with {@link Level#DEBUG} level.
   *
   * @param message The message
   */
  public void debug(String message);

  /**
   * Log error stacktrace with {@link Level#DEBUG} level.
   *
   * @param error The error
   */
  public void debug(Throwable error);

  /**
   * Log template message with {@link Level#DEBUG} level.
   *
   * @param messageTemplate The message template
   */
  public void debug(TextTemplate messageTemplate);

  /**
   * Log text message with {@link Level#ERROR} level.
   *
   * @param message The message
   */
  public void error(String message);

  /**
   * Log error stacktrace with {@link Level#ERROR} level.
   *
   * @param error The error
   */
  public void error(Throwable error);

  /**
   * Log template message with {@link Level#ERROR} level.
   *
   * @param messageTemplate The message template
   */
  public void error(TextTemplate messageTemplate);

  /**
   * Log text message with {@link Level#WARN} level.
   *
   * @param message The message
   */
  public void warn(String message);

  /**
   * Log error stacktrace with {@link Level#WARN} level.
   *
   * @param error The error
   */
  public void warn(Throwable error);

  /**
   * Log template message with {@link Level#WARN} level.
   *
   * @param messageTemplate The message template
   */
  public void warn(TextTemplate messageTemplate);

  /**
   * Log text message with {@link Level#INFO} level.
   *
   * @param message The message
   */
  public void info(String message);

  /**
   * Log error stacktrace with {@link Level#INFO} level.
   *
   * @param error The error
   */
  public void info(Throwable error);

  /**
   * Log template message with {@link Level#INFO} level.
   *
   * @param messageTemplate The message template
   */
  public void info(TextTemplate messageTemplate);

  /**
   * Log text message with {@link Level#CRITIC} level.
   *
   * @param message The message
   */
  public void critic(String message);

  /**
   * Log error stacktrace with {@link Level#CRITIC} level.
   *
   * @param error The error
   */
  public void critic(Throwable error);

  /**
   * Log template message with {@link Level#CRITIC} level.
   *
   * @param messageTemplate The message template
   */
  public void critic(TextTemplate messageTemplate);

  /**
   * Derive logger with specified name.
   *
   * @param loggerName The new logger name
   * @return The derived logger
   */
  public ApplicationLogger named(String loggerName);
}
