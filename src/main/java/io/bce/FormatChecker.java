package io.bce;

import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;

/**
 * This class is responsible for string values format checking.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public class FormatChecker {
  private final Pattern expressionPattern;
  private final ErrorFactory errorFactory;

  /**
   * Check that the string value is well-formatted.
   *
   * @param value The checked value
   */
  public void checkThatValueIsWellFormatted(String value) {
    if (!expressionPattern.matcher(value).matches()) {
      throw errorFactory.createError(value);
    }
  }

  /**
   * This interface declares the contract for the {@link RuntimeException} instances creating for
   * badly formatted string values.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  public interface ErrorFactory {
    public RuntimeException createError(String value);
  }

  public static final FormatChecker createFor(String expressionPattern) {
    return createFor(expressionPattern,
        value -> new WrongValueFormatException(value, expressionPattern));
  }

  public static final FormatChecker createFor(String expressionPattern, ErrorFactory errorFactory) {
    return new FormatChecker(Pattern.compile(expressionPattern), errorFactory);
  }

  /**
   * This exception is happened if a checked value is badly formatted.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  public static class WrongValueFormatException extends RuntimeException {
    private static final long serialVersionUID = -1953906038603832486L;

    /**
     * Create the exception.
     *
     * @param value   The badly formatted value
     * @param pattern The regex pattern which the vale should matched to.
     */
    public WrongValueFormatException(String value, String pattern) {
      super(String.format(
          "The \"%s\" value has wrong format. It must be matched to the \"%s\" pattern.", value,
          pattern));
    }
  }
}
