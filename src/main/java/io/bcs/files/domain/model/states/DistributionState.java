package io.bcs.files.domain.model.states;

import io.bcs.common.domain.model.io.transfer.CompletionCallback;
import io.bcs.common.domain.model.io.transfer.DestinationPoint;
import io.bcs.common.domain.model.io.transfer.SourcePoint;
import io.bcs.common.domain.model.io.transfer.TransferingScheduler;
import io.bcs.common.domain.model.io.transfer.Transmitter;
import io.bcs.files.domain.model.FileDownloadingContext;
import io.bcs.files.domain.model.FileState;
import io.bcs.files.domain.model.FileUploadingContext;
import io.bcs.files.domain.model.FilesystemAccessor;
import io.bcs.files.domain.model.errors.FileAlreadyExistsException;
import io.bcs.files.domain.model.errors.FileHasAlreadyBeenUploadedException;
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
	public void startDistribution(RootContext context) {
		throw new FileHasAlreadyBeenUploadedException();
	}

	@Override
	public void downloadFile(RootContext context, FileDownloadingContext downloadContext, Long offset, Long size) {		
		TransferingScheduler scheduler = downloadContext.getScheduler(); 
		DestinationPoint destination = downloadContext.getDestination();
		CompletionCallback callback = downloadContext.getCompletionCallback(); 
		FilesystemAccessor filesystemAccessor = downloadContext.getFileSystemAccessor();
		SourcePoint source = filesystemAccessor.getAccessOnRead(context.getFileName(), offset, size);
		Transmitter transmitter = scheduler.schedule(source, destination, callback);
		transmitter.start();
	}
}
