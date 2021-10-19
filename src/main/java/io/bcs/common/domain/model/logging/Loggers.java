package io.bcs.common.domain.model.logging;

import io.bcs.common.domain.model.logging.audit.ServiceAuditEvent;
import io.bcs.common.domain.model.logging.audit.ServiceAuditLogger;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Loggers {
	private ApplicationLogger applicationLogger = new NullApplicatioLogger();
	private ServiceAuditLogger eventsLogger = new NullEventsLogger();
	
	public final ApplicationLogger applicationLogger() {
		return applicationLogger;
	}
	
	public final ApplicationLogger applicationLogger(String loggerOwner) {
		return applicationLogger.named(loggerOwner);
	}
	
	public final ApplicationLogger applicationLogger(Class<?> loggerOwner) {
		return applicationLogger(loggerOwner.getName());
	}
	
	public final ServiceAuditLogger eventsLogger() {
		return eventsLogger;
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
	
	private static class NullApplicatioLogger implements ApplicationLogger {
		@Override
		public void log(LogRecord logRecord) {}

		@Override
		public ApplicationLogger named(String loggerName) {
			return this;
		}
	}
	
	private static class NullEventsLogger implements ServiceAuditLogger {
		@Override
		public void log(ServiceAuditEvent auditEvent) {}
	}
}
