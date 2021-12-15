package io.bcs.fileserver;

import io.bce.domain.BoundedContextId;
import io.bce.domain.errors.ErrorDescriptor.ErrorCode;

/**
 * This class defines the basic fileserver constants.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public class Constants {
  public static final BoundedContextId CONTEXT = BoundedContextId.createFor("BCS_FILESERVER");
  public static final ErrorCode PRIMARY_VALIDATION_ERROR = ErrorCode.createFor(1L);
  public static final ErrorCode FILE_IS_DISPOSED_ERROR = ErrorCode.createFor(2L);
  public static final ErrorCode CONTENT_IS_UPLOADED_ERROR = ErrorCode.createFor(3L);
  public static final ErrorCode FILE_STORAGE_INCIDENT_ERROR = ErrorCode.createFor(4L);
  public static final ErrorCode CONTENT_IS_NOT_UPLOADED_ERROR = ErrorCode.createFor(5L);
  public static final ErrorCode UNSATISFIABLE_RANGES_FORMAT_ERROR = ErrorCode.createFor(6L);
  public static final ErrorCode FILE_NOT_EXIST_ERROR = ErrorCode.createFor(7L);
  public static final ErrorCode FILE_IS_NOT_SPECIFIED = ErrorCode.createFor(8L);
}
