package io.bincloud.storage.domain.model.resource;

import io.bincloud.common.domain.model.io.transfer.CompletionCallback;
import io.bincloud.common.domain.model.io.transfer.SourcePoint;

public interface FileUploader {
	public void uploadFile(Long resourceId, SourcePoint source, CompletionCallback callback);
}
