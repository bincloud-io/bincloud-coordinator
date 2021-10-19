package io.bcs.files.application.download;

import java.util.function.Supplier;

import io.bcs.files.application.ExistingFileProvider;
import io.bcs.files.domain.model.File;
import io.bcs.files.domain.model.FileRepository;
import io.bcs.files.domain.model.contracts.FilePointer;
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
