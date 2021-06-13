package io.bincloud.storage.application;

import java.util.Optional;

import io.bincloud.common.domain.model.io.transfer.CompletionCallback;
import io.bincloud.common.domain.model.io.transfer.SourcePoint;
import io.bincloud.files.domain.model.contracts.FileStorage;
import io.bincloud.storage.domain.model.ResourceRepository;
import io.bincloud.storage.domain.model.contracts.FileUploader;
import io.bincloud.storage.domain.model.errors.ResourceDoesNotExistException;
import io.bincloud.storage.domain.model.file.FileUploadId;
import io.bincloud.storage.domain.model.file.FileUploadsHistory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileUploadService implements FileUploader {
	private final ResourceRepository resourceRepository;
	private final FileStorage fileStorage;
	private final FileUploadsHistory fileUploadsHistory;

	@Override
	public void uploadFile(Optional<Long> resourceId, SourcePoint source, UploadingCallback callback) {
		try {
			checkThatResourceExists(resourceId, callback);
			uploadFileToExistingResource(resourceId.get(), source, callback);
		} catch (Exception error) {
			callback.onError(error);
		}
	}
	
	private void uploadFileToExistingResource(Long resourceId, SourcePoint source, UploadingCallback callback) {
		String fileId = fileStorage.createNewFile();
		CompletionCallback completionCallback = createFileUploadingCompletionCallback(resourceId, fileId, callback);
		fileStorage.uploadFile(fileId, source, completionCallback);
	}
	
	
	private CompletionCallback createFileUploadingCompletionCallback(final Long resourceId, final String fileId, final UploadingCallback uploadingCallback) {
		return new CompletionCallback() {
			@Override
			public void onSuccess() {
				fileUploadsHistory.registerFileUpload(resourceId, fileId);
				uploadingCallback.onUpload(new FileUploadId(resourceId, fileId));
			}
			
			@Override
			public void onError(Exception error) {
				uploadingCallback.onError(error);
			}
		};
	}
	
	private void checkThatResourceExists(Optional<Long> resourceId, UploadingCallback callback) {
		resourceId.filter(value -> resourceRepository.isExists(value))
				.orElseThrow(() -> new ResourceDoesNotExistException());
	}
}
