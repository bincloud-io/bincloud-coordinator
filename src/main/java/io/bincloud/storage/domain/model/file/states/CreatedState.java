package io.bincloud.storage.domain.model.file.states;

import io.bincloud.storage.domain.model.file.FileAlreadyExistsException;
import io.bincloud.storage.domain.model.file.FileDownloadingContext;
import io.bincloud.storage.domain.model.file.FileHasNotBeenUploadedException;
import io.bincloud.storage.domain.model.file.FileState;
import io.bincloud.storage.domain.model.file.FileUploadingContext;
import io.bincloud.storage.domain.model.file.FilesystemAccessor;
import lombok.ToString;

public class CreatedState implements FileState {

	@Override
	@ToString.Include
	public String getStatus() {
		return FileStatus.CREATED.name();
	}

	@Override
	public FileState createFile(RootContext context, FilesystemAccessor fileSystem) {
		throw new FileAlreadyExistsException();
	}

	@Override
	public FileState uploadFile(RootContext context, FileUploadingContext uploadingContext) {
		uploadingContext.upload(context.getFileName());
		return this;
	}

	@Override
	public FileState startDistribution(RootContext context, FilesystemAccessor fileSystem) {
		context.setSize(fileSystem.getFileSize(context.getFileName()));
		return new DistributionState();
		
	}

	@Override
	public FileState downloadFile(RootContext context, FileDownloadingContext downloadingContext, Long offset, Long size) {
		throw new FileHasNotBeenUploadedException();
	}
}
