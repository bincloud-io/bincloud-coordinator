package io.bce.domain.errors;

import io.bce.domain.BoundedContextId;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * This interface describes an abstraction which provides data about happened error.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface ErrorDescriptor {
  /**
   * Get the bounded context identifier the error is assigned to.
   *
   * @return The bounded context identifier
   */
  public BoundedContextId getContextId();

  /**
   * Get the error code identifying an error into the bounded context.
   *
   * @return The error code
   */
  public ErrorCode getErrorCode();

  /**
   * Get the error severity showing it is a business error or an incident.
   *
   * @return The error severity
   */
  public ErrorSeverity getErrorSeverity();

  /**
   * Get the map of details parameters, describing the error.
   *
   * @return The details parameters map
   */
  public Map<String, Object> getErrorDetails();

  /**
   * This class enumerates of the error severity variants.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  public enum ErrorSeverity {
    /**
     * The incident error severity. It shows that the error happened because of an internal error
     * and it is not expected error.
     */
    INCIDENT,
    /**
     * The business error severity. It shows that the error happened because of incorrect input
     * parameters and it is expected error.
     */
    BUSINESS;
  }

  /**
   * This class represents the special error code. It should be used everywhere, when the value has
   * the error code into a bounded context semantic and not a random number value. It will prevent
   * the situation when the error code will be confused with another number value having another
   * semantic and will used for math calculations for example.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  @EqualsAndHashCode
  @RequiredArgsConstructor(staticName = "createFor")
  public class ErrorCode {
    public static final ErrorCode SUCCESSFUL_COMPLETED_CODE = ErrorCode.createFor(0L);
    public static final ErrorCode UNRECOGNIZED_ERROR_CODE = ErrorCode.createFor(-1L);

    @NonNull
    private final Long errorCode;

    /**
     * Extract the error code value.
     *
     * @return The error code value
     */
    public final Long extract() {
      return errorCode;
    }

    @Override
    public String toString() {
      return errorCode.toString();
    }
  }

}
