package io.bcs.storage.domain.model;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {
	public final String CONTEXT = "STORAGE";
	public final Long FILE_DOES_NOT_EXIST_EXCEPTION = 1L;
	public final Long FILE_ALREADY_EXISTS_EXCEPTION = 2L;
	public final Long FILE_HAS_NOT_BEEN_UPLOADED_EXCEPTION = 3L;
	public final Long FILE_HAS_ALREADY_BEEN_UPLOADED_EXCEPTION = 4L;
	public final Long FILE_HAS_ALREADY_BEEN_DISPOSED_EXCEPTION = 5L;
	public final Long UNSPECIFIED_FILESYSTEM_NAME_ERROR = 6L;
	public final Long FILE_REVISION_DOES_NOT_EXISTS_ERROR = 7L;
	public final Long UNSATISFIABLE_RANGE_FORMAT_ERROR = 8L;
}
