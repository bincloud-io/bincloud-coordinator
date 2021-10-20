package io.bcs.storage.domain.model;

import io.bce.domain.BoundedContextId;
import io.bce.domain.errors.ErrorDescriptor.ErrorCode;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {
	public final BoundedContextId CONTEXT = BoundedContextId.createFor("STORAGE");
	public final ErrorCode FILE_DOES_NOT_EXIST_EXCEPTION = ErrorCode.createFor(1L);
	public final ErrorCode FILE_ALREADY_EXISTS_EXCEPTION = ErrorCode.createFor(2L);
	public final ErrorCode FILE_HAS_NOT_BEEN_UPLOADED_EXCEPTION = ErrorCode.createFor(3L);
	public final ErrorCode FILE_HAS_ALREADY_BEEN_UPLOADED_EXCEPTION = ErrorCode.createFor(4L);
	public final ErrorCode FILE_HAS_ALREADY_BEEN_DISPOSED_EXCEPTION = ErrorCode.createFor(5L);
	public final ErrorCode UNSPECIFIED_FILESYSTEM_NAME_ERROR = ErrorCode.createFor(6L);
	public final ErrorCode FILE_REVISION_DOES_NOT_EXISTS_ERROR = ErrorCode.createFor(7L);
	public final ErrorCode UNSATISFIABLE_RANGE_FORMAT_ERROR = ErrorCode.createFor(8L);
}
