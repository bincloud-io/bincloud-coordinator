package io.bincloud.storage.application.file;

import java.util.Optional;

import io.bincloud.common.io.transfer.CompletionCallback;
import io.bincloud.common.io.transfer.DestinationPoint;
import io.bincloud.common.io.transfer.SourcePoint;
import io.bincloud.common.io.transfer.TransferingScheduler;
import io.bincloud.storage.domain.model.file.FileDescriptor;
import io.bincloud.storage.domain.model.file.FileRepository;
import io.bincloud.storage.domain.model.file.FileStorage;
import io.bincloud.storage.domain.model.file.FilesystemAccessor;
import io.bincloud.storage.domain.model.file.File.IdGenerator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileManagementService implements FileStorage {
	private final IdGenerator idGenerator;
	private final FileRepository fileRepository;
	private final FilesystemAccessor filesystemAccessor;
	private final TransferingScheduler transferingScheduler;

	@Override
	public String createNewFile() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<FileDescriptor> getFileDescriptor(String fileId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void uploadFile(String fileId, SourcePoint source, CompletionCallback callback) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void downloadFile(String fileId, DestinationPoint destination, CompletionCallback callback) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void downloadFileRange(String fileId, DestinationPoint destination, CompletionCallback callback, Long offset,
			Long size) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void disposeFile(String fileId) {
		throw new UnsupportedOperationException();
	}
}
