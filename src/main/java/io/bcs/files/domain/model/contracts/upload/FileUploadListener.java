package io.bcs.files.domain.model.contracts.upload;

import io.bcs.files.domain.model.contracts.FileDescriptor;

public interface FileUploadListener {
	public void onUpload(FileDescriptor fileDescriptor);
	public void onError(Exception error);
}
