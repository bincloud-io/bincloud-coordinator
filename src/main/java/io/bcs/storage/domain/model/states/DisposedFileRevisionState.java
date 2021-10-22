package io.bcs.storage.domain.model.states;

import io.bcs.storage.domain.model.FileRevision.FileRevisionState;
import io.bcs.storage.domain.model.contexts.FileDownloadingContext;
import io.bcs.storage.domain.model.contexts.FileUploadingContext;
import io.bcs.storage.domain.model.FilesystemAccessor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DisposedFileRevisionState implements FileRevisionState {
	@Override
	@ToString.Include
	@EqualsAndHashCode.Include
	public String getStatus() {
		return FileRevisionStatus.DISPOSED.name();
	}

	@Override
	public void createFile(RootContext context, FilesystemAccessor fileSystem) {
		throw new FileHasAlreadyBeenDisposedException();
	}

	@Override
	public void uploadFile(RootContext context, FileUploadingContext uploadingContext) {
		throw new FileHasAlreadyBeenDisposedException();
	}

	@Override
	public void downloadFile(RootContext context, FileDownloadingContext downloadingContext, Long offset, Long size) {
		throw new FileHasAlreadyBeenDisposedException();
	}

	@Override
	public void startDistribution(RootContext context) {
		throw new FileHasAlreadyBeenDisposedException();
	}
}
