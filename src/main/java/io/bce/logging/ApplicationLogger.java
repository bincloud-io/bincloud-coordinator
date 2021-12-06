package io.bce.logging;

import io.bce.text.TextTemplate;

public interface ApplicationLogger {
    public void log(LogRecord logRecord);

    /**
     * Log text message with {@link Level#TRACE} level
     * 
     * @param message The message
     */
    public void trace(String message);

    /**
     * Log template message with {@link Level#TRACE} level
     * 
     * @param messageTemplate The message template
     */
    public void trace(TextTemplate messageTemplate);

    /**
     * Log text message with {@link Level#DEBUG} level
     * 
     * @param message The message
     */
    public void debug(String message);

    /**
     * Log template message with {@link Level#DEBUG} level
     * 
     * @param messageTemplate The message template
     */
    public void debug(TextTemplate messageTemplate);

    /**
     * Log text message with {@link Level#ERROR} level
     * 
     * @param message The message
     */
    public void error(String message);

    /**
     * Log template message with {@link Level#ERROR} level
     * 
     * @param messageTemplate The message template
     */
    public void error(TextTemplate mmessageTemplate);

    /**
     * Log text message with {@link Level#WARN} level
     * 
     * @param message The message
     */
    public void warn(String message);

    /**
     * Log template message with {@link Level#WARN} level
     * 
     * @param messageTemplate The message template
     */
    public void warn(TextTemplate messageTemplate);

    /**
     * Log text message with {@link Level#INFO} level
     * 
     * @param message The message
     */
    public void info(String message);

    /**
     * Log template message with {@link Level#INFO} level
     * 
     * @param messageTemplate The message template
     */
    public void info(TextTemplate messageTemplate);

    /**
     * Log text message with {@link Level#CRITIC} level
     * 
     * @param message The message
     */
    public void critic(String message);

    /**
     * Log template message with {@link Level#CRITIC} level
     * 
     * @param messageTemplate The message template
     */
    public void critic(TextTemplate messageTemplate);
    
    /**
     * Derive logger with specified name
     * 
     * @param loggerName The new logger name
     * @return The derived logger
     */
    public ApplicationLogger named(String loggerName);
}
