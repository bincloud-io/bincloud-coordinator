package io.bincloud.resources.application

import java.time.Instant
import java.time.temporal.ChronoUnit

import io.bincloud.common.domain.model.error.ApplicationException.Severity
import io.bincloud.files.domain.model.FileDescriptor
import io.bincloud.files.domain.model.contracts.FileStorage
import io.bincloud.files.domain.model.states.FileStatus
import io.bincloud.resources.domain.model.Constants
import io.bincloud.resources.domain.model.errors.UploadedFileDescriptorHasNotBeenFoundException
import io.bincloud.resources.domain.model.file.FileUpload
import io.bincloud.resources.domain.model.file.FileUploadsHistory
import io.bincloud.resources.domain.model.file.FileUploadsRepository
import spock.lang.Narrative
import spock.lang.Specification

@Narrative("""
	In order to manage file uploads history, as a developer I'm needed in component
	implementing operations, defining in the FileUploadsHistory interface   
""")
class FileUploadsHistoryServiceSpec extends Specification {
	private static final Long RESOURCE_ID = 1L
	private static final String FILE_ID_1 = "file-id-1"
	private static final String FILE_ID_2 = "file-id-2"
	private static final Instant CREATION_MOMENT = Instant.now()
	private static final Instant LAST_MODIFICATION = CREATION_MOMENT.plus(1, ChronoUnit.MINUTES)
	private static final Long FILE_SIZE = 100L
	private static final String FILE_DISTRIBUTION_STATE = FileStatus.DISTRIBUTION.name()
	
	private FileStorage fileStorage;
	private FileUploadsRepository fileUploadsRepository;
	private FileUploadsHistory fileUploadsHistory;
	
	def setup() {
		this.fileStorage = Mock(FileStorage)
		this.fileUploadsRepository = Mock(FileUploadsRepository)
		this.fileUploadsHistory = new FileUploadsHistoryService(fileStorage, fileUploadsRepository)
	}
	
	def "Scenario: register file upload successfully"() {
		FileUpload fileUpload;
		given: "The existing file in the storage"
		FileDescriptor fileDescriptor = Stub(FileDescriptor)
		this.fileStorage.getFileDescriptor(FILE_ID_1) >> Optional.ofNullable(fileDescriptor)
		fileDescriptor.getCreationMoment() >> CREATION_MOMENT
		fileDescriptor.getLastModification() >> LAST_MODIFICATION
		fileDescriptor.getSize() >> FILE_SIZE
		fileDescriptor.getStatus() >> FILE_DISTRIBUTION_STATE
		
		when: "The file upload registers"
		this.fileUploadsHistory.registerFileUpload(RESOURCE_ID, FILE_ID_1)
		
		then: "The file upload should be successfully registered"
		1 * this.fileUploadsRepository.save(_) >> {fileUpload = it[0]}
		fileUpload.getResourceId() == RESOURCE_ID
		fileUpload.getFileId() == FILE_ID_1
		fileUpload.getUploadMoment() == LAST_MODIFICATION
	}
	
	def "Scenario: register file upload for unknown file"() {
		given: "The file doesn't exist in the storage"
		this.fileStorage.getFileDescriptor(FILE_ID_1) >> Optional.empty()
		
		when: "The file upload registers"
		this.fileUploadsHistory.registerFileUpload(RESOURCE_ID, FILE_ID_1)
		
		then: "The uploaded file descriptor has not been found exception should be thrown"
		UploadedFileDescriptorHasNotBeenFoundException error = thrown(UploadedFileDescriptorHasNotBeenFoundException)
		error.getContext() == Constants.CONTEXT
		error.getErrorCode() == UploadedFileDescriptorHasNotBeenFoundException.ERROR_CODE
		error.getSeverity() == Severity.INCIDENT
	}
	
	def "Scenario: truncate uploads history"() {
		given: "The two file uploads"
		fileUploadsRepository.findAllResourceUploads(RESOURCE_ID) >> ([
			createFileUpload(FILE_ID_1, LAST_MODIFICATION.plus(1, ChronoUnit.MINUTES)),
			createFileUpload(FILE_ID_2, LAST_MODIFICATION)
		]).stream()
		
		when: "The file history truncates to size of 1 element"
		fileUploadsHistory.truncateUploadsHistory(RESOURCE_ID, 1L)
		
		then: "The last file upload should be removed"
		1 * fileUploadsRepository.remove(RESOURCE_ID, FILE_ID_2)
		
		and: "The file associated with second upload should be disposed"
		1 * fileStorage.disposeFile(FILE_ID_2)
	}
	
	def "Scenario: clear uploads history"() {
		given: "The two file uploads"
		fileUploadsRepository.findAllResourceUploads(RESOURCE_ID) >> ([
			createFileUpload(FILE_ID_1, LAST_MODIFICATION.plus(1, ChronoUnit.MINUTES)),
			createFileUpload(FILE_ID_2, LAST_MODIFICATION)
		]).stream()
		
		when: "The file history clears"
		fileUploadsHistory.clearUploadsHistory(RESOURCE_ID)
		
		then: "The both file uploads should be removed"
		1 * fileUploadsRepository.remove(RESOURCE_ID, FILE_ID_1)
		1 * fileUploadsRepository.remove(RESOURCE_ID, FILE_ID_2)
		
		and: "The both files should be disposed in the storage"
		1 * fileStorage.disposeFile(FILE_ID_1)
		1 * fileStorage.disposeFile(FILE_ID_2)
	}
	
	def "Scenario: find lates uploaded file"() {
		given: "The repository contains file upload"
		FileUpload fileUpload = createFileUpload(FILE_ID_2, LAST_MODIFICATION)
		fileUploadsRepository.findLatestResourceUpload(RESOURCE_ID) >> Optional.of(fileUpload)
		
		expect: "The query will be delegated to the repository"
		fileUploadsHistory.findFileUploadForResource(RESOURCE_ID).get().is(fileUpload)
	}
	
	def "Scenario: check that file exists for existing file"() {
		given: "The repository contains file upload"
		FileUpload fileUpload = createFileUpload(FILE_ID_2, LAST_MODIFICATION)
		fileUploadsRepository.findById(RESOURCE_ID, FILE_ID_2) >> Optional.of(fileUpload)
		
		expect: "The file history service should answer that file exists"
		fileUploadsHistory.checkFileUploadExistence(RESOURCE_ID, FILE_ID_2) == true
	}
	
	def "Scenario: check that file doesn't exist for not existing file"() {
		given: "The repository contains file upload"
		fileUploadsRepository.findById(RESOURCE_ID, FILE_ID_2) >> Optional.empty()
		
		expect: "The file history service should answer that file doesn't exist"
		fileUploadsHistory.checkFileUploadExistence(RESOURCE_ID, FILE_ID_2) == false
	}
	
	private FileUpload createFileUpload(String fileId, Instant uploadMoment) {
		return FileUpload.builder()
		.resourceId(RESOURCE_ID)
		.fileId(fileId)
		.uploadMoment(uploadMoment)
		.build()
	}
}
