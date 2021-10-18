package io.bincloud.files.domain.model.contracts.upload;

import io.bincloud.common.domain.model.io.transfer.SourcePoint;
import io.bincloud.files.domain.model.contracts.FilePointer;

public interface FileUploader {
	public void uploadFileContent(FilePointer revisionPointer, Long contentSize, SourcePoint source, FileUploadListener callback);
}
