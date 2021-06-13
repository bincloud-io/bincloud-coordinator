package io.bincloud.files.domain.model.contracts;

import java.util.Optional;

import io.bincloud.common.domain.model.io.transfer.CompletionCallback;
import io.bincloud.common.domain.model.io.transfer.DestinationPoint;
import io.bincloud.common.domain.model.io.transfer.SourcePoint;
import io.bincloud.files.domain.model.FileDescriptor;

public interface FileStorage {
	public String createNewFile();

	public Optional<FileDescriptor> getFileDescriptor(String fileId);

	public void uploadFile(String fileId, SourcePoint source, CompletionCallback callback);

	public void downloadFile(String fileId, DestinationPoint destination, CompletionCallback callback);

	public void downloadFileRange(String fileId, DestinationPoint destination, CompletionCallback callback, Long offset,
			Long size);

	public void disposeFile(String fileId);
}
