package io.bincloud.storage.application.file;

import java.util.Optional;

import io.bincloud.common.io.transfer.CompletionCallback;
import io.bincloud.common.io.transfer.DestinationPoint;
import io.bincloud.common.io.transfer.SourcePoint;
import io.bincloud.storage.domain.model.file.FileDescriptor;
import io.bincloud.storage.domain.model.file.FileStorage;

public class FileManagementService implements FileStorage {
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
	public void disposeFile(String fileId) {
		throw new UnsupportedOperationException();
	}
}
