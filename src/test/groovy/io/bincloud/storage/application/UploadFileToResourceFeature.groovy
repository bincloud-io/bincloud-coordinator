package io.bincloud.storage.application

import java.time.temporal.ChronoUnit

import io.bincloud.common.domain.model.error.ApplicationException.Severity
import io.bincloud.common.domain.model.event.EventPublisher
import io.bincloud.common.domain.model.io.transfer.CompletionCallback
import io.bincloud.common.domain.model.io.transfer.SourcePoint
import io.bincloud.common.domain.model.time.DateTime
import io.bincloud.storage.application.resource.ResourceManagementService
import io.bincloud.storage.domain.model.file.FileDescriptor
import io.bincloud.storage.domain.model.file.FileStorage
import io.bincloud.storage.domain.model.file.states.FileStatus
import io.bincloud.storage.domain.model.resource.FileHasBeenUploaded
import io.bincloud.storage.domain.model.resource.FileUploader
import io.bincloud.storage.domain.model.resource.Resource
import io.bincloud.storage.domain.model.resource.ResourceDoesNotExistException
import io.bincloud.storage.domain.model.resource.ResourceRepository
import io.bincloud.storage.domain.model.resource.UploadedFileHasNotBeenFoundException
import io.bincloud.storage.domain.model.resource.file.FileUploadingRepository
import spock.lang.Narrative
import spock.lang.Specification

@Narrative("""
	In order to upload file to the storage,
	As a user, I want to upload the file to the physical storage and bind it with the existing resource.
""")
class UploadFileToResourceFeature extends Specification {
	private static final Long RESOURCE_ID = 1L
	private static final String FILE_ID = "12345"
	private static final DateTime CREATION_MOMENT = DateTime.now()
	private static final DateTime LAST_MODIFICATION = CREATION_MOMENT.plus(1, ChronoUnit.MINUTES)
	private static final Long FILE_SIZE = 100L

	private SourcePoint source;
	private ResourceRepository resourceRepository;
	private FileStorage fileStorage;
	private FileUploadingRepository fileUploadingRepository;
	private CompletionCallback completionCallback;
	private EventPublisher<FileHasBeenUploaded> fileHasBeenUploadedPublisher;
	private FileUploader fileUploader;

	def setup() {
		this.source = Stub(SourcePoint)
		this.resourceRepository = Mock(ResourceRepository)
		this.fileStorage = Mock(FileStorage)
		this.fileUploadingRepository = Mock(FileUploadingRepository)
		this.completionCallback = Mock(CompletionCallback)
		this.fileHasBeenUploadedPublisher = Mock(EventPublisher)
		this.fileUploader = new ResourceManagementService(resourceRepository, fileStorage, fileHasBeenUploadedPublisher)
	}

	def "Scenario: file is successfuly uploaded to the existing resource"() {
		given: "The resource exists in the repository"
		resourceRepository.isExists(RESOURCE_ID) >> true

		and: "The file storage will successfully create new resource"
		fileStorage.createNewFile() >> FILE_ID
		
		and: "The file descriptor will successfully found by file id"
		FileDescriptor fileDescriptor = Stub(FileDescriptor)
		fileDescriptor.getStatus() >> FileStatus.DISTRIBUTION.name()
		fileDescriptor.getSize() >> FILE_SIZE
		fileDescriptor.getCreationMoment() >> CREATION_MOMENT
		fileDescriptor.getLastModification() >> LAST_MODIFICATION
		fileStorage.getFileDescriptor(FILE_ID) >> Optional.of(fileDescriptor)

		and: "The file uploading will successfully completed"
		1 * fileStorage.uploadFile(FILE_ID, source, _) >> {
			CompletionCallback callback = it[2]
			callback.onSuccess()
		}

		when: "The file uploading is requested"
		fileUploader.uploadFile(RESOURCE_ID, source, completionCallback)
		
		then: "Completion callback should be successfully completed"
		1 * completionCallback.onSuccess()
		
		and: "System should be notified about file uploading to the resource"
		1 * fileHasBeenUploadedPublisher.publish(_) >> {
			FileHasBeenUploaded event = it[0]
			event.resourceId == RESOURCE_ID
			event.fileId == FILE_ID
			event.getUploadingMoment() == LAST_MODIFICATION
		}
	}
	
	def "Scenario: file is successfuly uploaded, but won't found by id"() {
		given: "The resource exists in the repository"
		resourceRepository.isExists(RESOURCE_ID) >> true

		and: "The file storage will successfully create new resource"
		fileStorage.createNewFile() >> FILE_ID
		
		and: "The file descriptor will successfully found by file id"
		fileStorage.getFileDescriptor(FILE_ID) >> Optional.empty()

		and: "The file uploading will successfully completed"
		1 * fileStorage.uploadFile(FILE_ID, source, _) >> {
			CompletionCallback callback = it[2]
			callback.onSuccess()
		}

		when: "The file uploading is requested"
		fileUploader.uploadFile(RESOURCE_ID, source, completionCallback)
		
		then: "The uploaded file has not been found has been thrown"
		UploadedFileHasNotBeenFoundException exception = thrown(UploadedFileHasNotBeenFoundException)
		exception.getContext() == UploadedFileHasNotBeenFoundException.CONTEXT
		exception.getErrorCode() == UploadedFileHasNotBeenFoundException.ERROR_CODE
	}

	def "Scenario: file uploading will be completed with resource does not exist error if resource not found"() {
		given: "The doesn't exist in the repository"
				resourceRepository.isExists(RESOURCE_ID) >> false
		
		when: "The file uploading is requested"
		fileUploader.uploadFile(RESOURCE_ID, source, completionCallback)
		
		then: "The file uploading was completed with resource does not exist error"
		1 * completionCallback.onError(_) >> {
			ResourceDoesNotExistException error = it[0]
			error.context == ResourceDoesNotExistException.CONTEXT
			error.errorCode == ResourceDoesNotExistException.ERROR_CODE
			error.severity == Severity.INCIDENT
		}
	}

	private def createResource() {
		return Resource.builder()
				.id(RESOURCE_ID)
				.build()
	}
}
