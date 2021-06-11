package io.bincloud.storage.domain.model.resource.file.history;

import java.util.Optional;

import io.bincloud.storage.domain.model.resource.file.FileUploading.InitialState;

public class FileUploadsHistory {
	public void registerFileUpload(Optional<InitialState> fileUploadingInitialState) {
	}

	public void truncateUploadsHistory(Long resourceId, Long count) {

	}
}
