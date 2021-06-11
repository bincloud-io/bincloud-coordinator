package io.bincloud.storage.domain.model.resource.file;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FileUploadingHasBeenRemoved {
	private Long resourceId;
	private String fileId;
}
