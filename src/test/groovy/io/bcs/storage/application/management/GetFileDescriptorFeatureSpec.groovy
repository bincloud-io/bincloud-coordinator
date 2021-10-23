package io.bcs.storage.application.management

import java.time.Instant
import java.time.temporal.ChronoUnit

import io.bcs.common.domain.model.generator.SequentialGenerator
import io.bcs.storage.application.management.FileManagementService
import io.bcs.storage.domain.model.FileRevision
import io.bcs.storage.domain.model.FileId
import io.bcs.storage.domain.model.FileRevisionRepository
import io.bcs.storage.domain.model.FilesystemAccessor
import io.bcs.storage.domain.model.contracts.FileDescriptor
import io.bcs.storage.domain.model.contracts.FileManager
import io.bcs.storage.domain.model.states.DistributionFileRevisionState
import spock.lang.Specification

class GetFileDescriptorFeatureSpec extends Specification {
	private static final FileId FILE_REVISION_NAME = new FileId("12345")
	private static final String FILE_NAME = "file.txt"
	private static final String FILE_MEDIA_TYPE = "application/media"
	private static final String FILE_DISPOSITION = "inline"
	private static final Long UPLOADED_FILE_SIZE = 1000000L
	private static final Instant TIMESTAMP_INITIAL_POINT = Instant.now()
	private static final Instant TIMESTAMP_NEXT_POINT = TIMESTAMP_INITIAL_POINT.plus(1, ChronoUnit.MILLIS)

	private FileRevisionRepository fileRepository
	private FileManager fileManager
	
	def setup() {
		this.fileRepository = Mock(FileRevisionRepository)
		this.fileManager = new FileManagementService(Stub(SequentialGenerator), fileRepository, Stub(FilesystemAccessor))
	}
	
	def "Scenario: get non empty descriptor for existing file"() {
		given: "The file exists in the repository"
		FileRevision file = createFile()
		fileRepository.findById(FILE_REVISION_NAME) >> Optional.of(file)
		
		when: "The file descriptor is requested by file id"
		Optional<FileDescriptor> optionalFileDescriptor = fileManager.getFileDescriptor(FILE_REVISION_NAME)
		
		then: "The non empty descriptor should be received"
		optionalFileDescriptor.isPresent() == true
		FileDescriptor fileDescriptor = optionalFileDescriptor.get()
		
		and: "The file descriptor state should be matched to file in the repository"
		fileDescriptor.getRevisionName() == file.getDescriptor().getRevisionName()
		fileDescriptor.getStatus() == file.getDescriptor().getStatus()
		fileDescriptor.getFileName() == file.getDescriptor().getFileName()
		fileDescriptor.getMediaType() == file.getDescriptor().getMediaType()
		fileDescriptor.getContentDisposition() == file.getDescriptor().getContentDisposition()
		fileDescriptor.getCreationMoment() == file.getDescriptor().getCreationMoment()
		fileDescriptor.getLastModification() == file.getDescriptor().getLastModification()
		fileDescriptor.getFileSize() == file.getDescriptor().getFileSize()
	}
	
	def "Scenario: get empty descriptor for not existing file"() {
		given: "The file doesn't exist in the repository"
		fileRepository.findById(FILE_REVISION_NAME) >> Optional.empty()
		
		when: "The file descriptor is requested by file id"
		Optional<FileDescriptor> optionalFileDescriptor = fileManager.getFileDescriptor(FILE_REVISION_NAME)
		
		then: "The empty descriptor should be received"
		optionalFileDescriptor.isPresent() == false
	}
	
	def createFile() {
		return FileRevision.builder()
				.revisionName(FILE_REVISION_NAME)
				.fileName(FILE_NAME)
				.mediaType(FILE_MEDIA_TYPE)
				.contentDisposition(FILE_DISPOSITION)
				.creationMoment(TIMESTAMP_INITIAL_POINT)
				.lastModification(TIMESTAMP_NEXT_POINT)
				.state(new DistributionFileRevisionState())
				.fileSize(1000L)
				.build()
	}
}
