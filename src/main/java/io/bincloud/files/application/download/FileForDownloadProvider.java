package io.bincloud.files.application.download;

import java.util.function.Supplier;

import io.bincloud.files.application.ExistingFileProvider;
import io.bincloud.files.domain.model.File;
import io.bincloud.files.domain.model.FileRepository;
import io.bincloud.files.domain.model.contracts.FilePointer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileForDownloadProvider implements Supplier<File> {
	private final FilePointer filePointer;
	private final FileRepository fileRepository;

	@Override
	public File get() {
		return findSpecifiedFile();

	}

	private File findSpecifiedFile() {
		return new ExistingFileProvider(filePointer, fileRepository).get();
	}
}
