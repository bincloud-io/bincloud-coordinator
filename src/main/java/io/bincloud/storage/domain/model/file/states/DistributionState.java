package io.bincloud.storage.domain.model.file.states;

import io.bincloud.storage.domain.model.file.FileAlreadyExistsException;
import io.bincloud.storage.domain.model.file.FileDownloadingContext;
import io.bincloud.storage.domain.model.file.FileHasAlreadyBeenUploadedException;
import io.bincloud.storage.domain.model.file.FileState;
import io.bincloud.storage.domain.model.file.FileUploadingContext;
import io.bincloud.storage.domain.model.file.FilesystemAccessor;
import lombok.ToString;

@ToString
public class DistributionState implements FileState {
	@Override
	@ToString.Include
	public String getStatus() {
		return FileStatus.DISTRIBUTION.name();
	}

	@Override
	public FileState createFile(RootContext context, FilesystemAccessor fileSystem) {
		throw new FileAlreadyExistsException();
	}

	@Override
	public FileState uploadFile(RootContext context, FileUploadingContext uploadingContext) {
		throw new FileHasAlreadyBeenUploadedException();
	}

	@Override
	public FileState startDistribution(RootContext context, FilesystemAccessor fileSystem) {
		throw new FileHasAlreadyBeenUploadedException();
	}

	@Override
	public FileState downloadFile(RootContext context, FileDownloadingContext downloadingContext, Long offset, Long size) {
		downloadingContext.download(context.getFileName(), offset, size);
		return this;
	}
}
