package io.bincloud.storage.application.resource.file;

import java.util.Optional;

import io.bincloud.common.domain.model.event.EventPublisher;
import io.bincloud.common.domain.model.io.transfer.CompletionCallback;
import io.bincloud.common.domain.model.io.transfer.SourcePoint;
import io.bincloud.storage.domain.model.file.FileDescriptor;
import io.bincloud.storage.domain.model.file.facades.FileStorage;
import io.bincloud.storage.domain.model.resource.ResourceRepository;
import io.bincloud.storage.domain.model.resource.errors.ResourceDoesNotExistException;
import io.bincloud.storage.domain.model.resource.errors.UploadedFileDescriptorHasNotBeenFoundException;
import io.bincloud.storage.domain.model.resource.facades.FileUploader;
import io.bincloud.storage.domain.model.resource.file.FileHasBeenUploaded;
import io.bincloud.storage.domain.model.resource.file.history.FileUploadsHistory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileUploadService implements FileUploader {
	private final ResourceRepository resourceRepository;
	private final FileStorage fileStorage;
	
	private final EventPublisher<FileHasBeenUploaded> fileHasBeenUploadedPublisher;

	@Override
	public void uploadFile(Optional<Long> resourceId, SourcePoint source, UploadingCallback callback) {
		try {
			checkThatResourceExists(resourceId, callback);
			uploadFileToExistingResource(resourceId.get(), source, callback);
		} catch (ResourceDoesNotExistException error) {
			callback.onError(error);
		}
	}
	
	private void uploadFileToExistingResource(Long resourceId, SourcePoint source, UploadingCallback callback) {
		String fileId = fileStorage.createNewFile();
		CompletionCallback completionCallback = createFileUploadingCompletionCallback(resourceId, fileId, callback);
		fileStorage.uploadFile(fileId, source, completionCallback);
	}
	
	private FileUploadsHistory fileUploadsHistory;
	
	private CompletionCallback createFileUploadingCompletionCallback(final Long resourceId, final String fileId, final UploadingCallback uploadingCallback) {
		return new CompletionCallback() {
			@Override
			public void onSuccess() {
				notifySystemAboutFileUploading(resourceId, fileId);
				completeUploading(resourceId, fileId, uploadingCallback);
			}
			
			@Override
			public void onError(Exception error) {
				uploadingCallback.onError(error);
			}
			
			private void notifySystemAboutFileUploading(Long resourceId, String fileId) {
				Optional<FileDescriptor> foundFileDescriptor = fileStorage.getFileDescriptor(fileId);
				if (foundFileDescriptor.isPresent()) {
					FileDescriptor fileDescriptor = foundFileDescriptor.get();
					fileHasBeenUploadedPublisher
						.publish(new FileHasBeenUploaded(resourceId, fileId, fileDescriptor.getLastModification()));
					
				} else {
					onError(new UploadedFileDescriptorHasNotBeenFoundException());
				}	
			}
			
			private void completeUploading(Long resourceId, String fileId, UploadingCallback uploadingCallback) {
				uploadingCallback.onUpload(new UploadedResource() {
					@Override
					public Long getResourceId() {
						return resourceId;
					}
					
					@Override
					public String getFileId() {
						return fileId;
					}
				});
			}
		};
	}
	
	private void checkThatResourceExists(Optional<Long> resourceId, UploadingCallback callback) {
		resourceId.filter(value -> resourceRepository.isExists(value))
				.orElseThrow(() -> new ResourceDoesNotExistException());
	}
}
