package io.bcs.storage.application.upload;

import io.bcs.common.domain.model.io.transfer.CompletionCallback;
import io.bcs.common.domain.model.io.transfer.SourcePoint;
import io.bcs.common.domain.model.io.transfer.TransferingScheduler;
import io.bcs.storage.application.ExistingFileProvider;
import io.bcs.storage.domain.model.FileRevision;
import io.bcs.storage.domain.model.FileRevisionRepository;
import io.bcs.storage.domain.model.FilesystemAccessor;
import io.bcs.storage.domain.model.contexts.FileUploadingContext;
import io.bcs.storage.domain.model.contracts.FilePointer;
import io.bcs.storage.domain.model.contracts.upload.FileUploadListener;
import io.bcs.storage.domain.model.contracts.upload.FileUploader;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileUploadService implements FileUploader {
	private final FileRevisionRepository fileRepository;
	private final FilesystemAccessor filesystemAccessor;
	private final TransferingScheduler transferingScheduler;

	@Override
	public void uploadFileContent(FilePointer revisionPointer, Long contentSize, SourcePoint source, FileUploadListener uploadListener) {
		try {
			FileRevision fileRevision = new ExistingFileProvider(revisionPointer, fileRepository).get();
			CompletionCallback uploadCompletionCallback = new FileUploadCompletionCallback(uploadListener, fileRevision);
			FileUploadingContext fileUploadContext = new FileUploadingContext(contentSize, source, transferingScheduler, filesystemAccessor, uploadCompletionCallback);
			fileRevision.uploadFileContent(fileUploadContext);
		} catch (Exception error) {
			uploadListener.onError(error);
		}
	}
	
	@RequiredArgsConstructor
	private class FileUploadCompletionCallback implements CompletionCallback {
		private final FileUploadListener uploadListener;
		private final FileRevision file;
		
		@Override
		public void onSuccess() {
			fileRepository.save(file);
			uploadListener.onUpload(file);
		}

		@Override
		public void onError(Exception error) {
			uploadListener.onError(error);
		}
	}
}
