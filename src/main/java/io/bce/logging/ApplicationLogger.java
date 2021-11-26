package io.bce.logging;

public interface ApplicationLogger {
	public void log(LogRecord logRecord);
	public ApplicationLogger named(String loggerName);
}
