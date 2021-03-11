package io.bincloud.storage.application.resource;

import io.bincloud.common.io.transfer.CompletionCallback;
import io.bincloud.common.io.transfer.SourcePoint;

public interface FileUploader {
	public void uploadFile(Long resourceId, SourcePoint source, CompletionCallback callback);
}
