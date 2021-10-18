package io.bincloud.files.domain.model.states;

import io.bincloud.common.domain.model.io.transfer.CompletionCallback;
import io.bincloud.common.domain.model.io.transfer.CompletionCallbackWrapper;
import io.bincloud.common.domain.model.io.transfer.DestinationPoint;
import io.bincloud.common.domain.model.io.transfer.SourcePoint;
import io.bincloud.common.domain.model.io.transfer.TransferingScheduler;
import io.bincloud.common.domain.model.io.transfer.Transmitter;
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
		SourcePoint source = uploadingContext.getSource();
		TransferingScheduler transferingScheduler = uploadingContext.getScheduler();
		FilesystemAccessor filesystemAccessor = uploadingContext.getFileSystemAccessor();
		CompletionCallback callback = new ContentUploadCallback(context, uploadingContext,
				uploadingContext.getCompletionCallback());
		DestinationPoint destination = filesystemAccessor.getAccessOnWrite(context.getFileName(),
				uploadingContext.getContentSize());
		Transmitter contentTransmitter = transferingScheduler.schedule(source, destination, callback);
		contentTransmitter.start();
	}

	@Override
	public void startDistribution(RootContext context) {
		context.setState(new DistributionState());
	}

	@Override
	public void downloadFile(RootContext context, FileDownloadingContext downloadingContext, Long offset, Long size) {
		throw new FileHasNotBeenUploadedException();
	}

	private class ContentUploadCallback extends CompletionCallbackWrapper {
		private final RootContext rootContext;
		private final FileUploadingContext uploadingContext;

		public ContentUploadCallback(RootContext rootContext, FileUploadingContext uploadingContext,
				CompletionCallback originalCallback) {
			super(originalCallback);
			this.rootContext = rootContext;
			this.uploadingContext = uploadingContext;
		}

		@Override
		public void onSuccess() {
			rootContext.setSize(uploadingContext.getContentSize());
			super.onSuccess();
		}
	}
}
