package io.bincloud.storage.domain.model.resource;

import io.bincloud.common.domain.model.time.DateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FileHasBeenUploaded {
	private Long resourceId;
	private String fileId;
	private DateTime uploadingMoment;
}
