package io.bcs.storage.application;

import java.util.function.Supplier;

import io.bcs.storage.domain.model.FileRevision;
import io.bcs.storage.domain.model.FileId;
import io.bcs.storage.domain.model.FileRevisionRepository;
import io.bcs.storage.domain.model.contracts.FilePointer;
import io.bcs.storage.domain.model.errors.UnspecifiedFilesystemNameException;
import io.bcs.storage.domain.model.states.FileDoesNotExistException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExistingFileProvider implements Supplier<FileRevision> {
	private final FileId replicaGroup;
	private final FileRevisionRepository fileRepository;

	public ExistingFileProvider(FilePointer filePointer, FileRevisionRepository fileRepository) {
		super();
		String fileId = filePointer.getFilesystemName().orElseThrow(UnspecifiedFilesystemNameException::new);
		this.replicaGroup = new FileId(fileId);
		this.fileRepository = fileRepository;
	}

	@Override
	public FileRevision get() {
		return fileRepository.findById(replicaGroup.getFilesystemName())
				.orElseThrow(FileDoesNotExistException::new);
	}
}
