package io.bcs.storage.domain.model.states;

import io.bce.promises.Promise;
import io.bcs.storage.domain.model.FileRevision.ContentUploader;
import io.bcs.storage.domain.model.FileRevision.ContentUploader.UploadedContent;
import io.bcs.storage.domain.model.FileRevision.FileRevisionState;
import io.bcs.storage.domain.model.FilesystemAccessor;
import io.bcs.storage.domain.model.contexts.FileDownloadingContext;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CreatedFileRevisionState implements FileRevisionState {

	@Override
	@ToString.Include
	@EqualsAndHashCode.Include
	public String getStatus() {
		return FileRevisionStatus.CREATED.name();
	}

	@Override
	public void createFile(RootContext context, FilesystemAccessor fileSystem) {
		throw new FileAlreadyExistsException();
	}

	@Override
	public Promise<UploadedContent> uploadContent(RootContext context, ContentUploader contentUploader) {
		return contentUploader.upload(context.getRevisionName());
	}

	@Override
	public void startDistribution(RootContext context) {
		context.setState(new DistributionFileRevisionState());
	}

	@Override
	public void downloadFile(RootContext context, FileDownloadingContext downloadingContext, Long offset, Long size) {
		throw new FileHasNotBeenUploadedException();
	}
}
