package io.bincloud.resources.application;

import java.util.Optional;
import java.util.function.Supplier;

import io.bincloud.common.domain.model.io.transfer.CompletionCallback;
import io.bincloud.common.domain.model.io.transfer.SourcePoint;
import io.bincloud.files.domain.model.contracts.FileStorage;
import io.bincloud.resources.application.providers.ExistingResourceIdentifierProvider;
import io.bincloud.resources.domain.model.ResourceRepository;
import io.bincloud.resources.domain.model.contracts.FileUploader;
import io.bincloud.resources.domain.model.file.FileUploadId;
import io.bincloud.resources.domain.model.file.FileUploadsHistory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileUploadService implements FileUploader {
	private final ResourceRepository resourceRepository;
	private final FileStorage fileStorage;
	private final FileUploadsHistory fileUploadsHistory;

	@Override
	public void uploadFile(Optional<Long> resourceId, SourcePoint source, UploadingCallback callback) {
		try {
			uploadFileToExistingResource(new ExistingResourceIdentifierProvider(resourceId, resourceRepository), source, callback);
		} catch (Exception error) {
			callback.onError(error);
		}
	}
	
	private void uploadFileToExistingResource(Supplier<Long> resourceIdProvider, SourcePoint source, UploadingCallback callback) {
		String fileId = fileStorage.createNewFile();
		CompletionCallback completionCallback = createFileUploadingCompletionCallback(resourceIdProvider.get(), fileId, callback);
		fileStorage.uploadFile(fileId, source, completionCallback);
	}
	
	
	private CompletionCallback createFileUploadingCompletionCallback(Long resourceId, String fileId, UploadingCallback uploadingCallback) {
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
}
