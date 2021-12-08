package io.bce.logging

import io.bce.domain.errors.ErrorStackTrace
import spock.lang.Specification

class AbstractLoggerSpec extends Specification {
  private static final String LOG_MESSAGE = "Hello world"

  def "Scenario: log trace message"() {
    given: "The abstract logger"
    StubLogger logger = new StubLogger()

    when: "The message is logged using level-separated method for trace level"
    logger.trace(LOG_MESSAGE)
    LogRecord loggedRecord = logger.getLastLogged()


    then: "The message should be logged with ${Level.TRACE}"
    loggedRecord.getLevel() == Level.TRACE
    loggedRecord.getMessageText() == LOG_MESSAGE
  }

  def "Scenario: log trace error"() {
    RuntimeException error = new RuntimeException("Something went wrong!!!")

    given: "The abstract logger"
    StubLogger logger = new StubLogger()

    when: "The error is logged using level-separated method for trace level"
    logger.trace(error)
    LogRecord loggedRecord = logger.getLastLogged()


    then: "The message should be logged with ${Level.TRACE}"
    loggedRecord.getLevel() == Level.TRACE
    loggedRecord.getMessageText() == new ErrorStackTrace(error).toString()
  }

  def "Scenario: log debug message"() {
    given: "The abstract logger"
    StubLogger logger = new StubLogger()

    when: "The message is logged using level-separated method for debug level"
    logger.debug(LOG_MESSAGE)
    LogRecord loggedRecord = logger.getLastLogged()


    then: "The message should be logged with ${Level.DEBUG}"
    loggedRecord.getLevel() == Level.DEBUG
    loggedRecord.getMessageText() == LOG_MESSAGE
  }

  def "Scenario: log debug error"() {
    RuntimeException error = new RuntimeException("Something went wrong!!!")

    given: "The abstract logger"
    StubLogger logger = new StubLogger()

    when: "The error is logged using level-separated method for debug level"
    logger.debug(error)
    LogRecord loggedRecord = logger.getLastLogged()


    then: "The message should be logged with ${Level.DEBUG}"
    loggedRecord.getLevel() == Level.DEBUG
    loggedRecord.getMessageText() == new ErrorStackTrace(error).toString()
  }

  def "Scenario: log info message"() {
    given: "The abstract logger"
    StubLogger logger = new StubLogger()

    when: "The message is logged using level-separated method for info level"
    logger.info(LOG_MESSAGE)
    LogRecord loggedRecord = logger.getLastLogged()


    then: "The message should be logged with ${Level.INFO}"
    loggedRecord.getLevel() == Level.INFO
    loggedRecord.getMessageText() == LOG_MESSAGE
  }

  def "Scenario: log info error"() {
    RuntimeException error = new RuntimeException("Something went wrong!!!")

    given: "The abstract logger"
    StubLogger logger = new StubLogger()

    when: "The error is logged using level-separated method for trace level"
    logger.info(error)
    LogRecord loggedRecord = logger.getLastLogged()


    then: "The message should be logged with ${Level.INFO}"
    loggedRecord.getLevel() == Level.INFO
    loggedRecord.getMessageText() == new ErrorStackTrace(error).toString()
  }

  def "Scenario: log warn message"() {
    given: "The abstract logger"
    StubLogger logger = new StubLogger()

    when: "The message is logged using level-separated method for warn level"
    logger.warn(LOG_MESSAGE)
    LogRecord loggedRecord = logger.getLastLogged()


    then: "The message should be logged with ${Level.WARN}"
    loggedRecord.getLevel() == Level.WARN
    loggedRecord.getMessageText() == LOG_MESSAGE
  }

  def "Scenario: log warn error"() {
    RuntimeException error = new RuntimeException("Something went wrong!!!")

    given: "The abstract logger"
    StubLogger logger = new StubLogger()

    when: "The error is logged using level-separated method for warn level"
    logger.warn(error)
    LogRecord loggedRecord = logger.getLastLogged()


    then: "The message should be logged with ${Level.WARN}"
    loggedRecord.getLevel() == Level.WARN
    loggedRecord.getMessageText() == new ErrorStackTrace(error).toString()
  }

  def "Scenario: log error message"() {
    given: "The abstract logger"
    StubLogger logger = new StubLogger()

    when: "The message is logged using level-separated method for error level"
    logger.error(LOG_MESSAGE)
    LogRecord loggedRecord = logger.getLastLogged()


    then: "The message should be logged with ${Level.ERROR}"
    loggedRecord.getLevel() == Level.ERROR
    loggedRecord.getMessageText() == LOG_MESSAGE
  }

  def "Scenario: log error level error"() {
    RuntimeException error = new RuntimeException("Something went wrong!!!")

    given: "The abstract logger"
    StubLogger logger = new StubLogger()

    when: "The error is logged using level-separated method for error level"
    logger.error(error)
    LogRecord loggedRecord = logger.getLastLogged()


    then: "The message should be logged with ${Level.ERROR}"
    loggedRecord.getLevel() == Level.ERROR
    loggedRecord.getMessageText() == new ErrorStackTrace(error).toString()
  }

  def "Scenario: log critic message"() {
    given: "The abstract logger"
    StubLogger logger = new StubLogger()

    when: "The message is logged using level-separated method for critic level"
    logger.critic(LOG_MESSAGE)
    LogRecord loggedRecord = logger.getLastLogged()


    then: "The message should be logged with ${Level.CRITIC}"
    loggedRecord.getLevel() == Level.CRITIC
    loggedRecord.getMessageText() == LOG_MESSAGE
  }

  def "Scenario: log critic level error"() {
    RuntimeException error = new RuntimeException("Something went wrong!!!")

    given: "The abstract logger"
    StubLogger logger = new StubLogger()

    when: "The error is logged using level-separated method for critic level"
    logger.critic(error)
    LogRecord loggedRecord = logger.getLastLogged()


    then: "The message should be logged with ${Level.CRITIC}"
    loggedRecord.getLevel() == Level.CRITIC
    loggedRecord.getMessageText() == new ErrorStackTrace(error).toString()
  }

  private static class StubLogger extends AbstractLogger {
    private LogRecord lastLogged;


    public LogRecord getLastLogged() {
      return lastLogged;
    }

    @Override
    public void log(LogRecord logRecord) {
      lastLogged = logRecord
    }

    @Override
    public ApplicationLogger named(String loggerName) {
      throw new UnsupportedOperationException();
    }
  }
}
