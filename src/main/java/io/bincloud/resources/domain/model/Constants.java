package io.bincloud.resources.domain.model;

import lombok.experimental.UtilityClass;


@UtilityClass
public class Constants {
	public final String CONTEXT = "RESOURCES";
	public final Long UNSPECIFIED_RESOURCE_ERROR = 1L;
	public final Long RESOURCE_DOES_NOT_EXIST_ERROR = 2L;
	public final Long RESOURCE_DOES_NOT_HAVE_UPLOADINGS_ERROR = 3L;
	public final Long UPLOADED_FILE_DESCRIPTOR_HAS_NOT_BEEN_FOUND_ERROR = 4L;
	public final Long INVALID_CREATE_NEW_RESOURCE_COMMAND_STATE = 5L;
	public final Long INVALID_REMOVE_EXISTING_RESOURCE_COMMAND_STATE = 6L;
	public final Long INVALID_CREATE_RESOURCE_FILE_UPLOAD_LINK_COMMAND_STATE = 7L;
}
