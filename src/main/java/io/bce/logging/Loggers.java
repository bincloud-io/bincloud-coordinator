package io.bce.logging;

import java.util.Optional;

import io.bce.logging.audit.ServiceAuditEvent;
import io.bce.logging.audit.ServiceAuditLogger;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Loggers {
  private ApplicationLogger applicationLogger = new NullApplicatioLogger();
  private ServiceAuditLogger eventsLogger = new NullEventsLogger();

  public final ApplicationLogger applicationLogger() {
    return new ApplicationLoggerProxy();
  }

  public final ApplicationLogger applicationLogger(String loggerOwner) {
    return applicationLogger().named(loggerOwner);
  }

  public final ApplicationLogger applicationLogger(Class<?> loggerOwner) {
    return applicationLogger(loggerOwner.getName());
  }

  public final ServiceAuditLogger eventsLogger() {
    return new ServiceAuditLoggerProxy();
  }

  public final Registry registry() {
    return new Registry() {
      @Override
      public Registry registerApplicationLogger(ApplicationLogger applicationLogger) {
        Loggers.applicationLogger = applicationLogger;
        return this;
      }

      @Override
      public Registry registerEventsLogger(ServiceAuditLogger eventsLogger) {
        Loggers.eventsLogger = eventsLogger;
        return this;
      }
    };
  }

  public interface Registry {
    public Registry registerApplicationLogger(ApplicationLogger applicationLogger);

    public Registry registerEventsLogger(ServiceAuditLogger eventsLogger);
  }

  private static class ApplicationLoggerProxy extends AbstractLogger {
    private Optional<String> loggerName = Optional.empty();

    @Override
    public void log(LogRecord logRecord) {
      getNamedLoggerInstance().log(logRecord);
    }

    @Override
    public ApplicationLogger named(String loggerName) {
      this.loggerName = Optional.of(loggerName);
      return this;
    }

    private ApplicationLogger getNamedLoggerInstance() {
      return loggerName.map(applicationLogger::named).orElse(applicationLogger);
    }
  }

  private static class ServiceAuditLoggerProxy implements ServiceAuditLogger {
    @Override
    public void log(ServiceAuditEvent auditEvent) {
      eventsLogger.log(auditEvent);
    }
  }

  private static class NullApplicatioLogger extends AbstractLogger {
    @Override
    public void log(LogRecord logRecord) {
    }

    @Override
    public ApplicationLogger named(String loggerName) {
      return this;
    }
  }

  private static class NullEventsLogger implements ServiceAuditLogger {
    @Override
    public void log(ServiceAuditEvent auditEvent) {
    }
  }
}
