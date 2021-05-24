package io.bincloud.storage.domain.model.resource;

import lombok.experimental.UtilityClass;


@UtilityClass
public class Constants {
	public final String CONTEXT = "STORAGE__RESOURCE_MANAGEMENT";
	public final Long RESOURCE_DOES_NOT_EXIST_ERROR = 2L;
	public final Long RESOURCE_DOES_NOT_HAVE_UPLOADINGS_ERROR = 3L;
	public final Long UPLOADED_FILE_DESCRIPTOR_HAS_NOT_BEEN_FOUND_ERROR = 4L;
	public final Long INVALID_RESOURCE_DETAILS_ERROR = 5L;
}
