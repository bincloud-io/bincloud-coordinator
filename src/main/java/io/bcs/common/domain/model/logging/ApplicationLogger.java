package io.bcs.common.domain.model.logging;

public interface ApplicationLogger {
	public void log(LogRecord logRecord);
	public ApplicationLogger named(String loggerName);
}