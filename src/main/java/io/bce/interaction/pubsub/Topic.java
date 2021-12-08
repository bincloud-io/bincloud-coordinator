package io.bce.interaction.pubsub;

import io.bce.FormatChecker;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * This class identifies the pub-sub topic.
 *
 * @author Dmitry Mikaylenko
 *
 */
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public final class Topic {
  private static final String PATTERN = "^[a-zA-Z0-9\\-\\_\\.]+$";
  private static final FormatChecker TOPIC_NAME_FORMAT_CHECKER =
      FormatChecker.createFor(PATTERN, WrongTopicNameFormatException::new);

  private final String topicName;

  @Override
  public String toString() {
    return topicName;
  }

  /**
   * Create the topic object.
   *
   * @param name The topic name string value
   * @return The topic
   */
  public static final Topic ofName(@NonNull String name) {
    TOPIC_NAME_FORMAT_CHECKER.checkThatValueIsWellFormatted(name);
    return new Topic(name);
  }

  /**
   * This exception is happened if a topic name is badly formatted.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  public static class WrongTopicNameFormatException extends RuntimeException {
    private static final long serialVersionUID = -3671386257899005216L;

    /**
     * Create the exception by a topic name string.
     *
     * @param topicName The topic name string
     */
    public WrongTopicNameFormatException(String topicName) {
      super(String.format(
          "The topic name \"%s\" has wrong format. It should be matched to the pattern \"%s\"",
          topicName, PATTERN));
    }
  }
}
