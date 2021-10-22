package io.bcs.storage.domain.model.states;

import io.bcs.storage.domain.model.FileRevision.FileRevisionState;
import io.bcs.storage.domain.model.contexts.FileDownloadingContext;
import io.bcs.storage.domain.model.contexts.FileUploadingContext;
import io.bcs.storage.domain.model.FilesystemAccessor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DraftFileRevisionState implements FileRevisionState {

	@Override
	@ToString.Include
	@EqualsAndHashCode.Include
	public String getStatus() {
		return FileRevisionStatus.DRAFT.name();
	}

	@Override
	public void createFile(RootContext context, FilesystemAccessor fileSystem) {
		fileSystem.createFile(context.getFileName());
		context.setState(new CreatedFileRevisionState());
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
