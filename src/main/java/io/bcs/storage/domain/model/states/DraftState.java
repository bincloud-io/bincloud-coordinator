package io.bcs.storage.domain.model.states;

import io.bcs.storage.domain.model.FileDownloadingContext;
import io.bcs.storage.domain.model.FileState;
import io.bcs.storage.domain.model.FileUploadingContext;
import io.bcs.storage.domain.model.FilesystemAccessor;
import io.bcs.storage.domain.model.errors.FileDoesNotExistException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DraftState implements FileState {

	@Override
	@ToString.Include
	@EqualsAndHashCode.Include
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
		throw new FileDoesNotExistException();
	}

	@Override
	public void startDistribution(RootContext context) {
		throw new FileDoesNotExistException();
	}

	@Override
	public void downloadFile(RootContext context, FileDownloadingContext downloadingContext, Long offset, Long size) {
		throw new FileDoesNotExistException();
	}
}
