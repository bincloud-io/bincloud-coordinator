package io.bincloud.files.domain.model.states;

import io.bincloud.files.domain.model.FileDownloadingContext;
import io.bincloud.files.domain.model.FileState;
import io.bincloud.files.domain.model.FileUploadingContext;
import io.bincloud.files.domain.model.FilesystemAccessor;
import io.bincloud.files.domain.model.errors.FileAlreadyExistsException;
import io.bincloud.files.domain.model.errors.FileHasNotBeenUploadedException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CreatedState implements FileState {

	@Override
	@ToString.Include
	@EqualsAndHashCode.Include
	public String getStatus() {
		return FileStatus.CREATED.name();
	}

	@Override
	public void createFile(RootContext context, FilesystemAccessor fileSystem) {
		throw new FileAlreadyExistsException();
	}

	@Override
	public void uploadFile(RootContext context, FileUploadingContext uploadingContext) {
		uploadingContext.upload(context.getFileName());
	}

	@Override
	public void startDistribution(RootContext context, FilesystemAccessor fileSystem) {
		context.setSize(fileSystem.getFileSize(context.getFileName()));
		context.setState(new DistributionState());
	}

	@Override
	public void downloadFile(RootContext context, FileDownloadingContext downloadingContext, Long offset, Long size) {
		throw new FileHasNotBeenUploadedException();
	}
}
