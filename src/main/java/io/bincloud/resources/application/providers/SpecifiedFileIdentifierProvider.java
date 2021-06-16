package io.bincloud.resources.application.providers;

import java.util.Optional;
import java.util.function.Supplier;

import io.bincloud.resources.domain.model.errors.ResourceDoesNotHaveUploadsException;
import io.bincloud.resources.domain.model.file.FileUploadsHistory;

public class SpecifiedFileIdentifierProvider implements Supplier<String> {
	private final Long resourceId;
	private final Optional<String> fileId;
	private final FileUploadsHistory fileUploadsHistory;

	public SpecifiedFileIdentifierProvider(Supplier<Long> resourceIdProvider, String fileId,
			FileUploadsHistory fileUploadsHistory) {
		super();
		this.fileId = Optional.of(fileId);
		this.resourceId = resourceIdProvider.get();
		this.fileUploadsHistory = fileUploadsHistory;
	}

	@Override
	public String get() {
		return fileId.filter(fileId -> fileUploadsHistory.checkFileUploadExistence(resourceId, fileId))
				.orElseThrow(() -> new ResourceDoesNotHaveUploadsException());
	}
}
