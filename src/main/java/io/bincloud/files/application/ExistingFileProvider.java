package io.bincloud.files.application;

import java.util.function.Supplier;

import io.bincloud.files.domain.model.File;
import io.bincloud.files.domain.model.FileId;
import io.bincloud.files.domain.model.FileRepository;
import io.bincloud.files.domain.model.contracts.FilePointer;
import io.bincloud.files.domain.model.errors.FileDoesNotExistException;
import io.bincloud.files.domain.model.errors.UnspecifiedFilesystemNameException;
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
