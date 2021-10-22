package io.bcs.storage.application.download;

import java.util.function.Supplier;

import io.bcs.storage.application.ExistingFileProvider;
import io.bcs.storage.domain.model.FileRevision;
import io.bcs.storage.domain.model.FileRevisionRepository;
import io.bcs.storage.domain.model.contracts.FilePointer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileForDownloadProvider implements Supplier<FileRevision> {
	private final FilePointer filePointer;
	private final FileRevisionRepository fileRepository;

	@Override
	public FileRevision get() {
		return findSpecifiedFile();

	}

	private FileRevision findSpecifiedFile() {
		return new ExistingFileProvider(filePointer, fileRepository).get();
	}
}
