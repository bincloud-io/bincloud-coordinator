package io.bcs.storage.domain.model.states;

import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bcs.storage.domain.model.FileRevision.ContentUploader;
import io.bcs.storage.domain.model.FileRevision.ContentUploader.UploadedContent;
import io.bcs.storage.domain.model.FileRevision.FileRevisionState;
import io.bcs.storage.domain.model.FilesystemAccessor;
import io.bcs.storage.domain.model.contexts.FileDownloadingContext;
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
		fileSystem.createFile(context.getRevisionName());
		context.setState(new CreatedFileRevisionState());
	}

	@Override
	public Promise<UploadedContent> uploadContent(RootContext context, ContentUploader contentUploader) {
		return Promises.rejectedBy(new FileDoesNotExistException());
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
