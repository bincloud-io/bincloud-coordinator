package io.bincloud.files.domain.model.contracts;

import java.util.Optional;

import io.bincloud.files.domain.model.FileId;
import io.bincloud.files.domain.model.contracts.upload.FileAttributes;

public interface FileManager {
	public Optional<FileDescriptor> getFileDescriptor(FileId fileId);
	
	public FileId createFileRevision(FileAttributes revisionDescriptor);
	
	public void disposeFile(FileId fileId);
}
