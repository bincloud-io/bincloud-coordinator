package io.bcs.storage.domain.model.contracts;

import java.util.Optional;

import io.bcs.storage.domain.model.FileId;
import io.bcs.storage.domain.model.contracts.upload.FileAttributes;

public interface FileManager {
	public Optional<FileDescriptor> getFileDescriptor(FileId fileId);
	
	public FileId createFileRevision(FileAttributes revisionDescriptor);
	
	public void disposeFile(FileId fileId);
}
