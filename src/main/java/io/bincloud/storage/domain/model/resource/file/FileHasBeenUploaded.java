package io.bincloud.storage.domain.model.resource.file;

import io.bincloud.common.domain.model.time.DateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@Deprecated
@AllArgsConstructor
public class FileHasBeenUploaded {
	private Long resourceId;
	private String fileId;
	private DateTime uploadingMoment;
}
