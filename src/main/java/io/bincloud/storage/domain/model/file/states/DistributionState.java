package io.bincloud.storage.domain.model.file.states;

import io.bincloud.storage.domain.model.file.FileDownloadingContext;
import io.bincloud.storage.domain.model.file.FileState;
import io.bincloud.storage.domain.model.file.FileUploadingContext;
import io.bincloud.storage.domain.model.file.FilesystemAccessor;
import io.bincloud.storage.domain.model.file.errors.FileAlreadyExistsException;
import io.bincloud.storage.domain.model.file.errors.FileHasAlreadyBeenUploadedException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DistributionState implements FileState {
	@Override
	@ToString.Include
	@EqualsAndHashCode.Include
	public String getStatus() {
		return FileStatus.DISTRIBUTION.name();
	}

	@Override
	public void createFile(RootContext context, FilesystemAccessor fileSystem) {
		throw new FileAlreadyExistsException();
	}

	@Override
	public void uploadFile(RootContext context, FileUploadingContext uploadingContext) {
		throw new FileHasAlreadyBeenUploadedException();
	}

	@Override
	public void startDistribution(RootContext context, FilesystemAccessor fileSystem) {
		throw new FileHasAlreadyBeenUploadedException();
	}

	@Override
	public void downloadFile(RootContext context, FileDownloadingContext downloadingContext, Long offset, Long size) {
		downloadingContext.download(context.getFileName(), offset, size);
	}
}
