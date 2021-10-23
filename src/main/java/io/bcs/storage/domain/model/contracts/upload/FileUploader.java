package io.bcs.storage.domain.model.contracts.upload;

import java.util.Optional;

import io.bce.interaction.streaming.binary.BinarySource;
import io.bce.promises.Promise;
import io.bcs.storage.domain.model.FileId;
import io.bcs.storage.domain.model.contracts.FileDescriptor;

public interface FileUploader {
	public Promise<FileDescriptor> uploadFileContent(UploadFileCommand uploadFileCommand, BinarySource source);
	
	
	public interface UploadFileCommand {
		public Optional<FileId> getRevisionName();
	}
}
