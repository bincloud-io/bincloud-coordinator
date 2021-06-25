package io.bincloud.resources.application;

import java.util.function.Supplier;

import io.bincloud.files.domain.model.FileDescriptor;
import io.bincloud.files.domain.model.contracts.FileStorage;
import io.bincloud.resources.domain.model.errors.UploadedFileDescriptorHasNotBeenFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExistingFileDescriptorProvider implements Supplier<FileDescriptor> {
	private final String fileId;
	private final FileStorage fileStorage;

	@Override
	public FileDescriptor get() {
		return fileStorage.getFileDescriptor(fileId)
				.orElseThrow(() -> new UploadedFileDescriptorHasNotBeenFoundException());
	}

}
