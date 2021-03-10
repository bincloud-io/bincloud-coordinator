package io.bincloud.storage.domain.model.file.states;

import io.bincloud.storage.domain.model.file.FileDownloadingContext;
import io.bincloud.storage.domain.model.file.FileNotExistsException;
import io.bincloud.storage.domain.model.file.FileState;
import io.bincloud.storage.domain.model.file.FileUploadingContext;
import io.bincloud.storage.domain.model.file.FilesystemAccessor;
import lombok.ToString;

public class DraftState implements FileState {

	@Override
	@ToString.Include
	public String getStatus() {
		return FileStatus.DRAFT.name();
	}

	@Override
	public FileState createFile(RootContext context, FilesystemAccessor fileSystem) {
		fileSystem.createFile(context.getFileName());
		return new CreatedState();
	}

	@Override
	public FileState uploadFile(RootContext context, FileUploadingContext uploadingContext) {
		throw new FileNotExistsException();
	}

	@Override
	public FileState startDistribution(RootContext context, FilesystemAccessor fileSystem) {
		throw new FileNotExistsException();
	}

	@Override
	public FileState downloadFile(RootContext context, FileDownloadingContext downloadingContext, Long offset, Long size) {
		throw new FileNotExistsException();
	}

}
