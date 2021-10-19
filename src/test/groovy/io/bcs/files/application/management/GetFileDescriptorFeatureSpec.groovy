package io.bcs.files.application.management

import java.time.Instant
import java.time.temporal.ChronoUnit

import io.bcs.common.domain.model.generator.SequentialGenerator
import io.bcs.files.application.management.FileManagementService
import io.bcs.files.domain.model.File
import io.bcs.files.domain.model.FileId
import io.bcs.files.domain.model.FileRepository
import io.bcs.files.domain.model.FilesystemAccessor
import io.bcs.files.domain.model.contracts.FileDescriptor
import io.bcs.files.domain.model.contracts.FileManager
import io.bcs.files.domain.model.states.DistributionState
import spock.lang.Specification

class GetFileDescriptorFeatureSpec extends Specification {
	private static final String FILESYSTEM_NAME = "12345"
	private static final String FILE_NAME = "file.txt"
	private static final String FILE_MEDIA_TYPE = "application/media"
	private static final String FILE_DISPOSITION = "inline"
	private static final Long UPLOADED_FILE_SIZE = 1000000L
	private static final Instant TIMESTAMP_INITIAL_POINT = Instant.now()
	private static final Instant TIMESTAMP_NEXT_POINT = TIMESTAMP_INITIAL_POINT.plus(1, ChronoUnit.MILLIS)
	private static final FileId FILE_ID = new FileId(FILESYSTEM_NAME)

	private FileRepository fileRepository
	private FileManager fileManager
	
	def setup() {
		this.fileRepository = Mock(FileRepository)
		this.fileManager = new FileManagementService(Stub(SequentialGenerator), fileRepository, Stub(FilesystemAccessor))
	}
	
	def "Scenario: get non empty descriptor for existing file"() {
		given: "The file exists in the repository"
		File file = createFile()
		fileRepository.findById(FILESYSTEM_NAME) >> Optional.of(file)
		
		when: "The file descriptor is requested by file id"
		Optional<FileDescriptor> optionalFileDescriptor = fileManager.getFileDescriptor(FILE_ID)
		
		then: "The non empty descriptor should be received"
		optionalFileDescriptor.isPresent() == true
		FileDescriptor fileDescriptor = optionalFileDescriptor.get()
		
		and: "The file descriptor state should be matched to file in the repository"
		fileDescriptor.getFilesystemName() == file.getFilesystemName()
		fileDescriptor.getStatus() == file.getStatus()
		fileDescriptor.getFileName() == file.getFileName()
		fileDescriptor.getMediaType() == file.getMediaType()
		fileDescriptor.getContentDisposition() == file.getContentDisposition()
		fileDescriptor.getCreationMoment() == file.getCreationMoment()
		fileDescriptor.getLastModification() == file.getLastModification()
		fileDescriptor.getFileSize() == file.getFileSize()
	}
	
	def "Scenario: get empty descriptor for not existing file"() {
		given: "The file doesn't exist in the repository"
		fileRepository.findById(FILESYSTEM_NAME) >> Optional.empty()
		
		when: "The file descriptor is requested by file id"
		Optional<FileDescriptor> optionalFileDescriptor = fileManager.getFileDescriptor(FILE_ID)
		
		then: "The empty descriptor should be received"
		optionalFileDescriptor.isPresent() == false
	}
	
	def createFile() {
		return File.builder()
				.filesystemName(FILESYSTEM_NAME)
				.fileName(FILE_NAME)
				.mediaType(FILE_MEDIA_TYPE)
				.contentDisposition(FILE_DISPOSITION)
				.creationMoment(TIMESTAMP_INITIAL_POINT)
				.lastModification(TIMESTAMP_NEXT_POINT)
				.state(new DistributionState())
				.fileSize(1000L)
				.build()
	}
}
