package io.bcs.files.application.management;

import java.util.Optional;

import io.bcs.common.domain.model.generator.SequentialGenerator;
import io.bcs.files.application.ExistingFileProvider;
import io.bcs.files.domain.model.File;
import io.bcs.files.domain.model.FileId;
import io.bcs.files.domain.model.FileRepository;
import io.bcs.files.domain.model.FilesystemAccessor;
import io.bcs.files.domain.model.contracts.FileDescriptor;
import io.bcs.files.domain.model.contracts.FileManager;
import io.bcs.files.domain.model.contracts.upload.FileAttributes;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileManagementService implements FileManager {
	private final SequentialGenerator<String> filesystemNameGenerator;
	private final FileRepository fileRepository;
	private final FilesystemAccessor filesystemAccessor;

	@Override
	public Optional<FileDescriptor> getFileDescriptor(FileId fileId) {
		return fileRepository.findById(fileId.getFilesystemName()).map(file -> file);
	}

	@Override
	public FileId createFileRevision(FileAttributes fileAttributes) {
		File file = new File(filesystemNameGenerator, fileAttributes);
		file.createFile(filesystemAccessor);
		fileRepository.save(file);
		return new FileId(file.getFilesystemName());
	}

	@Override
	public void disposeFile(FileId replicaGroup) {
		File file = new ExistingFileProvider(replicaGroup, fileRepository).get();
		file.dispose(filesystemAccessor);
		fileRepository.save(file);
	}
}
