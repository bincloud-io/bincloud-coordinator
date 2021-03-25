package io.bincloud.storage.domain.model.file.states;

import io.bincloud.storage.domain.model.file.FileDownloadingContext;
import io.bincloud.storage.domain.model.file.FileNotExistException;
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
	public void createFile(RootContext context, FilesystemAccessor fileSystem) {
		fileSystem.createFile(context.getFileName());
		context.setState(new CreatedState());
	}

	@Override
	public void uploadFile(RootContext context, FileUploadingContext uploadingContext) {
		throw new FileNotExistException();
	}

	@Override
	public void startDistribution(RootContext context, FilesystemAccessor fileSystem) {
		throw new FileNotExistException();
	}

	@Override
	public void downloadFile(RootContext context, FileDownloadingContext downloadingContext, Long offset, Long size) {
		throw new FileNotExistException();
	}
}
