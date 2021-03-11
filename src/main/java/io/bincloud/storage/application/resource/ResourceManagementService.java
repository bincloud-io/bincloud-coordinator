package io.bincloud.storage.application.resource;

import io.bincloud.common.event.EventPublisher;
import io.bincloud.common.io.transfer.CompletionCallback;
import io.bincloud.common.io.transfer.SourcePoint;
import io.bincloud.storage.domain.model.file.FileStorage;
import io.bincloud.storage.domain.model.resource.FileHasBeenUploaded;
import io.bincloud.storage.domain.model.resource.ResourceRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ResourceManagementService implements FileUploader {
	private final ResourceRepository resourceRepository;
	private final FileStorage fileStorage;
	private final EventPublisher<FileHasBeenUploaded> fileHasBeenUploadedPublisher;
	
	@Override
	public void uploadFile(Long resourceId, SourcePoint source, CompletionCallback callback) {
		throw new UnsupportedOperationException();
	}
}
