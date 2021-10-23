package io.bcs.storage.domain.model.states;

import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bcs.common.domain.model.io.transfer.CompletionCallback;
import io.bcs.common.domain.model.io.transfer.DestinationPoint;
import io.bcs.common.domain.model.io.transfer.SourcePoint;
import io.bcs.common.domain.model.io.transfer.TransferingScheduler;
import io.bcs.common.domain.model.io.transfer.Transmitter;
import io.bcs.storage.domain.model.FileRevision.ContentUploader;
import io.bcs.storage.domain.model.FileRevision.ContentUploader.UploadedContent;
import io.bcs.storage.domain.model.FileRevision.FileRevisionState;
import io.bcs.storage.domain.model.FilesystemAccessor;
import io.bcs.storage.domain.model.contexts.FileDownloadingContext;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DistributionFileRevisionState implements FileRevisionState {
	@Override
	@ToString.Include
	@EqualsAndHashCode.Include
	public String getStatus() {
		return FileRevisionStatus.DISTRIBUTION.name();
	}

	@Override
	public void createFile(RootContext context, FilesystemAccessor fileSystem) {
		throw new FileAlreadyExistsException();
	}

	@Override
	public Promise<UploadedContent> uploadContent(RootContext context, ContentUploader contentUploader) {
		return Promises.rejectedBy(new FileHasAlreadyBeenUploadedException());	
	}	
	
//	@Override
//	public void uploadFile(RootContext context, FileUploadingContext uploadingContext) {
//		throw new FileHasAlreadyBeenUploadedException();
//	}

	@Override
	public void startDistribution(RootContext context) {
		throw new FileHasAlreadyBeenUploadedException();
	}

	@Override
	public void downloadFile(RootContext context, FileDownloadingContext downloadContext, Long offset, Long size) {		
		TransferingScheduler scheduler = downloadContext.getScheduler(); 
		DestinationPoint destination = downloadContext.getDestination();
		CompletionCallback callback = downloadContext.getCompletionCallback(); 
		FilesystemAccessor filesystemAccessor = downloadContext.getFileSystemAccessor();
		SourcePoint source = filesystemAccessor.getAccessOnRead(context.getRevisionName(), offset, size);
		Transmitter transmitter = scheduler.schedule(source, destination, callback);
		transmitter.start();
	}
}
