package io.bincloud.resources.application.providers;

import java.util.function.Supplier;

import io.bincloud.resources.domain.model.errors.ResourceDoesNotHaveUploadsException;
import io.bincloud.resources.domain.model.file.FileUpload;
import io.bincloud.resources.domain.model.file.FileUploadsHistory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UnspecifiedFileIdentifierProvider implements Supplier<String> {
	private final Supplier<Long> resourceIdProvider;
	private final FileUploadsHistory fileUploadsHistory;
	
	@Override
	public String get() {
		return fileUploadsHistory.findFileUploadForResource(resourceIdProvider.get()).map(FileUpload::getFileId)
				.orElseThrow(() -> new ResourceDoesNotHaveUploadsException());
	}
}
