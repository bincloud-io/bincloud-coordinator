package io.bincloud.files.domain.model.states;

import io.bincloud.files.domain.model.FileDownloadingContext;
import io.bincloud.files.domain.model.FileState;
import io.bincloud.files.domain.model.FileUploadingContext;
import io.bincloud.files.domain.model.FilesystemAccessor;
import io.bincloud.files.domain.model.errors.FileNotExistException;
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
