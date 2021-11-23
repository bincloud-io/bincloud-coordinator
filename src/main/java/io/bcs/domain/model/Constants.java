package io.bcs.domain.model;

import io.bce.domain.BoundedContextId;
import io.bce.domain.errors.ErrorDescriptor.ErrorCode;

public class Constants {
    public static final BoundedContextId CONTEXT = BoundedContextId.createFor("BCS_FILESERVER");
    public static final ErrorCode FILE_IS_DISPOSED_ERROR = ErrorCode.createFor(1L);
    public static final ErrorCode FILE_HAS_BEEN_UPLOADED_ERROR = ErrorCode.createFor(2L);
    public static final ErrorCode FILE_CONTENT_UPLOADING_ERROR = ErrorCode.createFor(3L);
    public static final ErrorCode FILE_CONTENT_DOWNLOADING_ERROR = ErrorCode.createFor(4L);
    public static final ErrorCode FILE_STORAGE_ERROR = ErrorCode.createFor(3L);

}
