package io.bcs.files.domain.model.contracts.upload;

import io.bcs.common.domain.model.io.transfer.SourcePoint;
import io.bcs.files.domain.model.contracts.FilePointer;

public interface FileUploader {
	public void uploadFileContent(FilePointer revisionPointer, Long contentSize, SourcePoint source, FileUploadListener callback);
}
