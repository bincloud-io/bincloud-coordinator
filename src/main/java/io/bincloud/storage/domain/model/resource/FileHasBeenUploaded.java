package io.bincloud.storage.domain.model.resource;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FileHasBeenUploaded {
	private Long resourceId;
	private String fileId;
}
