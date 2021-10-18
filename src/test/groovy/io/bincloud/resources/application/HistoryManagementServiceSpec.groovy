package io.bincloud.resources.application

import static io.bincloud.resources.domain.model.resource.history.UploadedFile.AvailabilityStatus.NOT_AVAILABLE

import java.time.Instant

import io.bincloud.resources.application.ResourceManagementService.RegisterFileUploadCommand
import io.bincloud.resources.domain.model.FileReference
import io.bincloud.resources.domain.model.resource.history.FileHistory
import io.bincloud.resources.domain.model.resource.history.FileHistoryService
import io.bincloud.resources.domain.model.resource.history.FileStorage
import io.bincloud.resources.domain.model.resource.history.UploadedFile
import io.bincloud.resources.domain.model.resource.history.UploadedFileRepository
import io.bincloud.resources.domain.model.resource.history.FileStorage.CreateFileInStorage
import io.bincloud.resources.domain.model.resource.history.UploadedFile.AvailabilityStatus
import spock.lang.Specification

class HistoryManagementServiceSpec extends Specification {
	private static final Long RESOURCE_ID = 1L
	private static final String FILE_NAME = "filename.txt"
	private static final String RANDOM_FILE_NAME = "RANDOM.FILE"
	private static final String APPLICATION_MEDIA_TYPE = "application/octet-stream";
	private static final String APPLICATION_CONTENT_DISPOSITION = "inline";
	private static final String FILESYSTEM_NAME = UUID.randomUUID().toString()
	private static final Instant CREATED_MOMENT = Instant.now()
	private static final Instant LAST_MODIFICATION_MOMENT = CREATED_MOMENT.plusSeconds(10)
	private static final Long FILE_SIZE = 1000L

	private UploadedFileRepository fileUploadsRepository
	private FileHistory historyManager
	private FileStorage fileStorage

	def setup() {
		this.fileStorage = Mock(FileStorage)
		this.fileUploadsRepository = Mock(UploadedFileRepository)
		this.historyManager = new FileHistoryService(fileUploadsRepository, fileStorage)
	}

	def "Scenario: register file upload successfully"() {
		UploadedFile fileUpload
		given: "The create file upload command"
		RegisterFileUploadCommand command = createFileUploadRegistrationCommand(RESOURCE_ID)

		and: "The file storage file creation completes successfully"
		1 * fileStorage.createFile(_) >> {
			CreateFileInStorage createFileCommand = it[0]
			return createFileReference(createFileCommand.getResourceId(), FILESYSTEM_NAME)
		}

		when: "The file upload creates"
		FileReference fileReference = historyManager.registerUploadedFile(command)

		then: "The file upload should be stored into the repository in the not available state"
		1 * fileUploadsRepository.save(_) >> {fileUpload = it[0]}
		fileUpload.getResourceId() == RESOURCE_ID
		fileUpload.getFilesystemName() == FILESYSTEM_NAME
		fileUpload.getAvailability() == AvailabilityStatus.NOT_AVAILABLE
	}
//
//	def "Scenario: register file upload for storage error"() {
//		RuntimeException error = new RuntimeException("ERROR")
//		given: "The create file upload command"
//		RegisterFileUploadCommand command = createFileUploadRegistrationCommand(RESOURCE_ID)
//
//		and: "The file storage file creation completes with error"
//		fileStorage.createFile(_) >> {throw error}
//
//		when: "The file upload creates"
//		FileReference fileReference = historyManager.registerFileUpload(command)
//
//		then: "The file upload shouldn't be stored into the repository"
//		0 * fileUploadsRepository.save(_)
//
//		and: "The error should be re-thrown"
//		RuntimeException thrownError = thrown()
//		thrownError == error
//	}
//
//	def "Scenario: activate existing file upload"() {
//		UploadedFile storedFileUpload
//		given: "The file upload activation command"
//		StartFileDistributionCommand command = createActivateFileUploadCommand(RESOURCE_ID, FILESYSTEM_NAME)
//		
//		and: "The not available file upload exists into repository"
//		fileUploadsRepository.findById(RESOURCE_ID, FILESYSTEM_NAME) >> Optional.of(UploadedFile.builder()
//			.resourceId(RESOURCE_ID)
//			.filesystemName(FILESYSTEM_NAME)
//			.availability(NOT_AVAILABLE)
//			.build())
//		
//		when: "The file upload is activated"
//		historyManager.activateFileUpload(command)
//		
//		then: "The file upload should be stored"
//		1 * fileUploadsRepository.save(_) >> {storedFileUpload = it[0]}
//		storedFileUpload.getResourceId() == RESOURCE_ID
//		storedFileUpload.getFilesystemName() == FILESYSTEM_NAME
//		
//		and: "The file upload should become available"
//		storedFileUpload.getAvailability() == AvailabilityStatus.AVAILABLE
//		
//		and: "The file size, creation moment and last modification shoudl be updated"
//		storedFileUpload.getFi
//		
//	}
//
//	def "Scenario: activate unknown file upload"() {
//		given: "The file upload does not exist into repository"
//	}
//	
//	private StartFileDistributionCommand createActivateFileUploadCommand(Long resourceId, String filesystemName) {
//		StartFileDistributionCommand command = Stub(StartFileDistributionCommand)
//		command.getReference() >> createFileReference(resourceId, filesystemName)
//		command.getCreatedMoment() >> CREATED_MOMENT
//		command.getLastModification() >> LAST_MODIFICATION_MOMENT
//		command.getFileSize() >> FILE_SIZE
//		return command
//	}

	private RegisterFileUploadCommand createFileUploadRegistrationCommand(Long resourceId) {
		RegisterFileUploadCommand command = Stub(RegisterFileUploadCommand)
		command.getResourceId() >> RESOURCE_ID
		command.getFileName() >> FILE_NAME
		command.getMediaType() >> APPLICATION_MEDIA_TYPE
		command.getContentDisposition() >> APPLICATION_CONTENT_DISPOSITION
		return command
	}

	private FileReference createFileReference(Long resourceId, String filesystemName) {
		FileReference fileReference = Stub(FileReference)
		fileReference.getResourceId() >> resourceId
		fileReference.getFilesystemName() >> filesystemName
		return fileReference
	}
}
