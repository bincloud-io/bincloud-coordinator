package io.bcs.storage.application;

import java.util.function.Supplier;

import io.bcs.storage.domain.model.FileId;
import io.bcs.storage.domain.model.FileRevision;
import io.bcs.storage.domain.model.FileRevisionRepository;
import io.bcs.storage.domain.model.contracts.FilePointer;
import io.bcs.storage.domain.model.errors.UnspecifiedRevisionNameException;
import io.bcs.storage.domain.model.states.FileDoesNotExistException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExistingFileProvider implements Supplier<FileRevision> {
	private final FileId replicaGroup;
	private final FileRevisionRepository fileRepository;
	

	public ExistingFileProvider(FilePointer filePointer, FileRevisionRepository fileRepository) {
		super();
		this.replicaGroup = filePointer.getFilesystemName().orElseThrow(UnspecifiedRevisionNameException::new);
		this.fileRepository = fileRepository;
	}

	@Override
	public FileRevision get() {
		return fileRepository.findById(replicaGroup)
				.orElseThrow(FileDoesNotExistException::new);
	}
}
