package io.bcs.storage.application;

import java.util.function.Supplier;

import io.bcs.storage.domain.model.File;
import io.bcs.storage.domain.model.FileId;
import io.bcs.storage.domain.model.FileRepository;
import io.bcs.storage.domain.model.contracts.FilePointer;
import io.bcs.storage.domain.model.errors.FileDoesNotExistException;
import io.bcs.storage.domain.model.errors.UnspecifiedFilesystemNameException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExistingFileProvider implements Supplier<File> {
	private final FileId replicaGroup;
	private final FileRepository fileRepository;

	public ExistingFileProvider(FilePointer filePointer, FileRepository fileRepository) {
		super();
		String fileId = filePointer.getFilesystemName().orElseThrow(UnspecifiedFilesystemNameException::new);
		this.replicaGroup = new FileId(fileId);
		this.fileRepository = fileRepository;
	}

	@Override
	public File get() {
		return fileRepository.findById(replicaGroup.getFilesystemName())
				.orElseThrow(FileDoesNotExistException::new);
	}
}
