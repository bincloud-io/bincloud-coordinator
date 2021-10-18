package io.bincloud.files.domain.model.contracts.upload;

import io.bincloud.files.domain.model.contracts.FileDescriptor;

public interface FileUploadListener {
	public void onUpload(FileDescriptor fileDescriptor);
	public void onError(Exception error);
}
