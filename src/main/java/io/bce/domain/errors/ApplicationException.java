package io.bce.domain.errors;

import io.bce.domain.BoundedContextId;
import java.util.HashMap;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

/**
 * This class represents the exception, assigned to your application logic. If you are going to
 * create your own exception, assigned to your domain model, you should extend from this class.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class ApplicationException extends RuntimeException implements ErrorDescriptor {
  private static final long serialVersionUID = 49273931466407615L;
  public static final String ERROR_MESSAGE_DETAIL_NAME = "$errorMessage";
  public static final String ERROR_STACKTRACE_DETAIL_NAME = "$errorStacktrace";

  @Getter
  @EqualsAndHashCode.Include
  private final BoundedContextId contextId;

  @Getter
  @EqualsAndHashCode.Include
  private final ErrorSeverity errorSeverity;

  @Getter
  @EqualsAndHashCode.Include
  private final ErrorCode errorCode;

  /**
   * Create the application exception.
   *
   * @param contextId     The bounded context id
   * @param errorSeverity The error severity
   * @param errorCode     The error code
   * @param errorMessage  The error message
   */
  public ApplicationException(@NonNull BoundedContextId contextId,
      @NonNull ErrorSeverity errorSeverity, @NonNull ErrorCode errorCode, String errorMessage) {
    super(errorMessage);
    this.contextId = contextId;
    this.errorSeverity = errorSeverity;
    this.errorCode = errorCode;
  }

  public ApplicationException(@NonNull BoundedContextId contextId,
      @NonNull ErrorSeverity errorSeverity, @NonNull ErrorCode errorCode) {
    this(contextId, errorSeverity, errorCode, "");

  }

  /**
   * If you are going to increase error details you should create derived map, containing all
   * parameters, returned by your superclass and then increase it. Don't ignore parameters, returned
   * by the superclass method. It might affect your application logic.
   */
  @Override
  @EqualsAndHashCode.Include
  public Map<String, Object> getErrorDetails() {
    Map<String, Object> errorDetails = new HashMap<>();
    errorDetails.put(ERROR_MESSAGE_DETAIL_NAME, getMessage());
    errorDetails.put(ERROR_STACKTRACE_DETAIL_NAME, new ErrorStackTrace(this));
    return errorDetails;
  }

  @Override
  public final String toString() {
    return getMessage();
  }
}
