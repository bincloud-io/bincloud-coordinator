package io.bce.domain.errors;

import java.util.HashMap;
import java.util.Map;

import io.bce.domain.BoundedContextId;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

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

	public ApplicationException(@NonNull BoundedContextId contextId, @NonNull ErrorSeverity errorSeverity,
			@NonNull ErrorCode errorCode, String errorMessage) {
		super(errorMessage);
		this.contextId = contextId;
		this.errorSeverity = errorSeverity;
		this.errorCode = errorCode;
	}

	public ApplicationException(@NonNull BoundedContextId contextId, @NonNull ErrorSeverity errorSeverity,
			@NonNull ErrorCode errorCode) {
		this(contextId, errorSeverity, errorCode, "");

	}

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
