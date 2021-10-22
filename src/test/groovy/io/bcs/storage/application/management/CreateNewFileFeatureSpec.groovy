package io.bcs.storage.application.management

import io.bcs.common.domain.model.generator.SequentialGenerator
import io.bcs.storage.application.management.FileManagementService
import io.bcs.storage.domain.model.FileRevision
import io.bcs.storage.domain.model.FileId
import io.bcs.storage.domain.model.FileRevisionRepository
import io.bcs.storage.domain.model.FilesystemAccessor
import io.bcs.storage.domain.model.contracts.FileManager
import io.bcs.storage.domain.model.contracts.upload.FileAttributes
import io.bcs.storage.domain.model.states.FileRevisionStatus
import spock.lang.Specification

class CreateNewFileFeatureSpec extends Specification {
	private static final String FILESYSTEM_NAME = "12345"
	private static final String FILE_NAME = "file.txt"
	private static final String FILE_MEDIA_TYPE = "application/media"
	private static final String FILE_DISPOSITION = "inline"
	
	private SequentialGenerator<String> filesystemNameGenerator
	private FileRevisionRepository fileRepository
	private FilesystemAccessor filesystemAccessor
	private FileManager fileManager
	private FileAttributes fileAttributes
	
	def setup() {
		this.filesystemNameGenerator = Stub(SequentialGenerator)
		this.fileRepository = Mock(FileRevisionRepository)
		this.filesystemAccessor = Mock(FilesystemAccessor)
		this.fileAttributes = Stub(FileAttributes)
		this.fileManager = new FileManagementService(filesystemNameGenerator, fileRepository, filesystemAccessor);
	}
	
	def "Scenario: create a new file"() {
		FileRevision file;
		given: "The filesystem name generator generates unique name"
		filesystemNameGenerator.nextValue() >> FILESYSTEM_NAME
		
		and: "The file attributes"
		fileAttributes.getFileName() >> FILE_NAME
		fileAttributes.getMediaType() >> FILE_MEDIA_TYPE
		fileAttributes.getContentDisposition() >> FILE_DISPOSITION
		
		when: "The file is created"
		FileId fileId = fileManager.createFileRevision(fileAttributes)
		
		then: "The file entity should be created and stored to the repository in created state"
		1 * fileRepository.save(_) >> {file = it[0]}
		file.getStatus() == FileRevisionStatus.CREATED.name()
		
		and: "The file should be created on a filesystem"
		1 * this.filesystemAccessor.createFile(FILESYSTEM_NAME)
		
		and: "The file id should contain correct values"
		fileId.getFilesystemName() == file.getFilesystemName()
	}
}
