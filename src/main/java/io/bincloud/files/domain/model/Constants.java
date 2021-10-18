package io.bincloud.files.domain.model;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {
	public final String CONTEXT = "STORAGE";
	public final Long UNSPECIFIED_FILESYSTEM_NAME_ERROR = 2L;
	public final Long FILE_REVISION_DOES_NOT_EXISTS_ERROR = 3L;
	public final Long UNSATISFIABLE_RANGE_FORMAT_ERROR = 4L;
}
