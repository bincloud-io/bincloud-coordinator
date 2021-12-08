package io.bce.logging;

import io.bce.domain.errors.ErrorStackTrace;
import io.bce.text.TextTemplate;
import io.bce.text.TextTemplates;

/**
 * This class is the base logger implementation which implements all logging functional which
 * doesn't depends on choosen logging framework. If you are going to implement additional logger,
 * you should extends this class in the most cases.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public abstract class AbstractLogger implements ApplicationLogger {
  public AbstractLogger() {
    super();
  }

  @Override
  public final void trace(String message) {
    trace(TextTemplates.createBy(message));
  }

  @Override
  public final void trace(Throwable error) {
    trace(buildErrorInfo(error));
  }

  @Override
  public final void trace(TextTemplate messageTemplate) {
    log(new LogRecord(Level.TRACE, messageTemplate));
  }

  @Override
  public final void debug(String message) {
    debug(TextTemplates.createBy(message));
  }

  @Override
  public final void debug(Throwable error) {
    debug(buildErrorInfo(error));
  }

  @Override
  public final void debug(TextTemplate messageTemplate) {
    log(new LogRecord(Level.DEBUG, messageTemplate));
  }

  @Override
  public final void info(String message) {
    info(TextTemplates.createBy(message));
  }

  @Override
  public final void info(Throwable error) {
    info(buildErrorInfo(error));
  }

  @Override
  public final void info(TextTemplate messageTemplate) {
    log(new LogRecord(Level.INFO, messageTemplate));
  }

  @Override
  public final void warn(String message) {
    warn(TextTemplates.createBy(message));
  }

  @Override
  public final void warn(Throwable error) {
    warn(buildErrorInfo(error));
  }

  @Override
  public final void warn(TextTemplate messageTemplate) {
    log(new LogRecord(Level.WARN, messageTemplate));
  }

  @Override
  public final void error(String message) {
    error(TextTemplates.createBy(message));
  }

  @Override
  public final void error(Throwable error) {
    error(buildErrorInfo(error));
  }

  @Override
  public final void error(TextTemplate messageTemplate) {
    log(new LogRecord(Level.ERROR, messageTemplate));
  }

  @Override
  public final void critic(String message) {
    critic(TextTemplates.createBy(message));
  }

  @Override
  public final void critic(Throwable error) {
    critic(buildErrorInfo(error));
  }

  @Override
  public final void critic(TextTemplate messageTemplate) {
    log(new LogRecord(Level.CRITIC, messageTemplate));
  }

  private String buildErrorInfo(Throwable error) {
    return new ErrorStackTrace(error).toString();
  }
}