package io.bincloud.files.domain.model.states;

import io.bincloud.files.domain.model.FileDownloadingContext;
import io.bincloud.files.domain.model.FileState;
import io.bincloud.files.domain.model.FileUploadingContext;
import io.bincloud.files.domain.model.FilesystemAccessor;
import io.bincloud.files.domain.model.errors.FileHasAlreadyBeenDisposedException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DisposedState implements FileState {
	@Override
	@ToString.Include
	@EqualsAndHashCode.Include
	public String getStatus() {
		return FileStatus.DISPOSED.name();
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
