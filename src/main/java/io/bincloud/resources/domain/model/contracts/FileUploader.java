package io.bincloud.resources.domain.model.contracts;

import java.util.Optional;

import io.bincloud.common.domain.model.io.transfer.SourcePoint;
import io.bincloud.resources.domain.model.file.FileUploadId;

public interface FileUploader {
	public void uploadFile(Optional<Long> resourceId, SourcePoint source, UploadingCallback callback);
	
	public interface UploadingCallback {
		public void onUpload(FileUploadId uploaded);
		public void onError(Exception error);
	}
}
