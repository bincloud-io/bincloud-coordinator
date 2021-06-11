package io.bincloud.storage.domain.model.resource.facades;

import java.util.Optional;

import io.bincloud.common.domain.model.io.transfer.SourcePoint;

public interface FileUploader {
	public void uploadFile(Optional<Long> resourceId, SourcePoint source, UploadingCallback callback);
	
	public interface UploadingCallback {
		public void onUpload(UploadedResource uploaded);
		public void onError(Exception error);
	}
	
	public interface UploadedResource {
		public Long getResourceId();
		public String getFileId();
	}
}
