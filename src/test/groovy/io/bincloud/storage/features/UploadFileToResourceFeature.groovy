package io.bincloud.storage.features

import io.bincloud.common.ApplicationException.Severity
import io.bincloud.common.event.EventPublisher
import io.bincloud.common.io.transfer.CompletionCallback
import io.bincloud.common.io.transfer.SourcePoint
import io.bincloud.storage.application.resource.FileUploader
import io.bincloud.storage.application.resource.ResourceDoesNotExistException
import io.bincloud.storage.application.resource.ResourceManagementService
import io.bincloud.storage.domain.model.file.FileStorage
import io.bincloud.storage.domain.model.resource.FileHasBeenUploaded
import io.bincloud.storage.domain.model.resource.Resource
import io.bincloud.storage.domain.model.resource.ResourceRepository
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
		}
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
