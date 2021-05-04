package io.bincloud.storage.application.resource;

import io.bincloud.common.domain.model.event.EventPublisher;
import io.bincloud.common.domain.model.io.transfer.CompletionCallback;
import io.bincloud.common.domain.model.io.transfer.CompletionCallbackWrapper;
import io.bincloud.common.domain.model.io.transfer.SourcePoint;
import io.bincloud.storage.domain.model.file.FileStorage;
import io.bincloud.storage.domain.model.resource.FileHasBeenUploaded;
import io.bincloud.storage.domain.model.resource.FileUploader;
import io.bincloud.storage.domain.model.resource.ResourceDoesNotExistException;
import io.bincloud.storage.domain.model.resource.ResourceRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ResourceManagementService implements FileUploader {
	private final ResourceRepository resourceRepository;
	private final FileStorage fileStorage;
	private final EventPublisher<FileHasBeenUploaded> fileHasBeenUploadedPublisher;

	@Override
	public void uploadFile(Long resourceId, SourcePoint source, CompletionCallback callback) {
		checkThatResourceExists(resourceId, callback);
		String fileId = fileStorage.createNewFile();
		fileStorage.uploadFile(fileId, source, new CompletionCallbackWrapper(callback) {
			@Override
			public void onSuccess() {
				fileHasBeenUploadedPublisher.publish(new FileHasBeenUploaded(resourceId, fileId));
				super.onSuccess();
			}
		});
	}

	private void checkThatResourceExists(Long resourceId, CompletionCallback callback) {
		if (!resourceRepository.isExists(resourceId)) {
			callback.onError(new ResourceDoesNotExistException());
		}
	}
}

