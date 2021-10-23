package io.bcs.storage.application.management;

import java.util.Optional;

import io.bcs.common.domain.model.generator.SequentialGenerator;
import io.bcs.storage.application.ExistingFileProvider;
import io.bcs.storage.domain.model.FileRevision;
import io.bcs.storage.domain.model.FileId;
import io.bcs.storage.domain.model.FileRevisionRepository;
import io.bcs.storage.domain.model.FilesystemAccessor;
import io.bcs.storage.domain.model.contracts.FileDescriptor;
import io.bcs.storage.domain.model.contracts.FileManager;
import io.bcs.storage.domain.model.contracts.upload.FileAttributes;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileManagementService implements FileManager {
	private final SequentialGenerator<FileId> filesystemNameGenerator;
	private final FileRevisionRepository fileRepository;
	private final FilesystemAccessor filesystemAccessor;

	@Override
	public Optional<FileDescriptor> getFileDescriptor(FileId fileId) {
		return fileRepository.findById(fileId).map(FileRevision::getDescriptor);
	}

	@Override
	public FileId createFileRevision(FileAttributes fileAttributes) {
		FileRevision fileRevision = new FileRevision(filesystemNameGenerator, fileAttributes);
		fileRevision.createFile(filesystemAccessor);
		fileRepository.save(fileRevision);
		return fileRevision.getRevisionName();
	}

	@Override
	public void disposeFile(FileId replicaGroup) {
		FileRevision fileRevision = new ExistingFileProvider(replicaGroup, fileRepository).get();
		fileRevision.dispose(filesystemAccessor);
		fileRepository.save(fileRevision);
	}
}
