package io.bincloud.files.application.management;

import java.util.Optional;

import io.bincloud.common.domain.model.generator.SequentialGenerator;
import io.bincloud.files.application.ExistingFileProvider;
import io.bincloud.files.domain.model.File;
import io.bincloud.files.domain.model.FileId;
import io.bincloud.files.domain.model.FileRepository;
import io.bincloud.files.domain.model.FilesystemAccessor;
import io.bincloud.files.domain.model.contracts.FileManager;
import io.bincloud.files.domain.model.contracts.FileDescriptor;
import io.bincloud.files.domain.model.contracts.upload.FileAttributes;
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
