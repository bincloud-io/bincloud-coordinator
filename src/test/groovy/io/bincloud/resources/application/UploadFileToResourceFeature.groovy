package io.bincloud.resources.application

import java.time.Instant
import java.time.temporal.ChronoUnit

import io.bincloud.common.domain.model.error.ApplicationException.Severity
import io.bincloud.common.domain.model.io.transfer.CompletionCallback
import io.bincloud.common.domain.model.io.transfer.SourcePoint
import io.bincloud.files.domain.model.FileDescriptor
import io.bincloud.files.domain.model.contracts.FileStorage
import io.bincloud.files.domain.model.states.FileStatus
import io.bincloud.resources.application.FileUploadService
import io.bincloud.resources.domain.model.Constants
import io.bincloud.resources.domain.model.Resource
import io.bincloud.resources.domain.model.ResourceRepository
import io.bincloud.resources.domain.model.contracts.FileUploader
import io.bincloud.resources.domain.model.contracts.FileUploader.UploadingCallback
import io.bincloud.resources.domain.model.errors.ResourceDoesNotExistException
import io.bincloud.resources.domain.model.errors.UnspecifiedResourceException
import io.bincloud.resources.domain.model.errors.UploadedFileDescriptorHasNotBeenFoundException
import io.bincloud.resources.domain.model.file.FileUploadId
import io.bincloud.resources.domain.model.file.FileUploadsHistory
import io.bincloud.resources.domain.model.file.FileUploadsRepository
import spock.lang.Narrative
import spock.lang.Specification

@Narrative("""
	In order to upload file to the storage,
	As a user, I want to upload the file to the physical storage and bind it with the existing resource.
""")
class UploadFileToResourceFeature extends Specification {
	private static final Long RESOURCE_ID = 1L
	private static final Optional<Long> EXISTING_RESOURCE_ID = Optional.of(RESOURCE_ID)
	private static final Optional<Long> MISSING_RESOURCE_ID = Optional.empty()
	private static final String FILE_ID = "12345"
	private static final String FILE_NAME = "file.txt"
	private static final Instant CREATION_MOMENT = Instant.now()
	private static final Instant LAST_MODIFICATION = CREATION_MOMENT.plus(1, ChronoUnit.MINUTES)
	private static final Long FILE_SIZE = 100L

	private SourcePoint source;
	private ResourceRepository resourceRepository;
	private FileStorage fileStorage;
	private FileUploadsRepository fileUploadingRepository;
	private UploadingCallback uploadingCallback;
	private FileUploader fileUploader;
	private FileUploadsHistory fileUploadsHistory;

	def setup() {
		this.source = Stub(SourcePoint)
		this.resourceRepository = Mock(ResourceRepository)
		this.fileStorage = Mock(FileStorage)
		this.fileUploadingRepository = Mock(FileUploadsRepository)
		this.uploadingCallback = Mock(UploadingCallback)
		this.fileUploadsHistory = Mock(FileUploadsHistory)		
		this.fileUploader = new FileUploadService(resourceRepository, fileStorage, fileUploadsHistory)
	}

	def "Scenario: file is successfuly uploaded to the existing resource"() {
		FileUploadId uploadedResource;
		given: "The resource exists in the repository"
		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())
		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())

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
		fileUploader.uploadFile(EXISTING_RESOURCE_ID, source, uploadingCallback)

		then: "Completion callback should be successfully completed"
		1 * uploadingCallback.onUpload(_) >> {uploadedResource = it[0]}
		uploadedResource.getResourceId() == RESOURCE_ID
		uploadedResource.getFileId() == FILE_ID
		
		and: "The file upload has been registered"
		1 * fileUploadsHistory.registerFileUpload(RESOURCE_ID, FILE_ID)
	}
	
	def "Scenario: something went wrong during file uploadig"() {
		Exception error;
		given: "The resource exists in the repository"
		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())

		and: "The file storage will successfully create new resource"
		fileStorage.createNewFile() >> FILE_ID

		and: "The file descriptor is successfully found by file id"
		FileDescriptor fileDescriptor = Stub(FileDescriptor)
		fileDescriptor.getStatus() >> FileStatus.DISTRIBUTION.name()
		fileDescriptor.getSize() >> FILE_SIZE
		fileDescriptor.getCreationMoment() >> CREATION_MOMENT
		fileDescriptor.getLastModification() >> LAST_MODIFICATION
		fileStorage.getFileDescriptor(FILE_ID) >> Optional.of(fileDescriptor)

		and: "The file upload is completed with error on the storage"
		1 * fileStorage.uploadFile(FILE_ID, source, _) >> {
			CompletionCallback callback = it[2]
			callback.onError(new Exception("ERROR"))
		}

		when: "The file uploading is requested by uploading callback"
		fileUploader.uploadFile(EXISTING_RESOURCE_ID, source, uploadingCallback)

		then: "The error should be received"
		1 * uploadingCallback.onError(_) >> {error = it[0]}
		error.getMessage() == "ERROR"
	}

	def "Scenario: file is successfuly uploaded, but file descriptor won't found by id"() {
		UploadedFileDescriptorHasNotBeenFoundException exception;
		given: "The resource exists in the repository"
		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())

		and: "The file storage will successfully create new resource"
		fileStorage.createNewFile() >> FILE_ID

		and: "The file descriptor will successfully found by file id"
		fileUploadsHistory.registerFileUpload(RESOURCE_ID, FILE_ID) >> {throw new UploadedFileDescriptorHasNotBeenFoundException()}

		and: "The file uploading will successfully completed"
		1 * fileStorage.uploadFile(FILE_ID, source, _) >> {
			CompletionCallback callback = it[2]
			callback.onSuccess()
		}

		when: "The file uploading is requested"
		fileUploader.uploadFile(EXISTING_RESOURCE_ID, source, uploadingCallback)

		then: "The uploaded file has not been found should be thrown"
		1 * uploadingCallback.onError(_) >> {exception = it[0]}
		exception.getContext() == Constants.CONTEXT
		exception.getErrorCode() == UploadedFileDescriptorHasNotBeenFoundException.ERROR_CODE
	}

	def "Scenario: resource does not exist error if resource not found"() {
		ResourceDoesNotExistException error;
		given: "The doesn't exist in the repository"
		resourceRepository.findById(RESOURCE_ID) >> Optional.empty()

		when: "The file uploading is requested"
		fileUploader.uploadFile(EXISTING_RESOURCE_ID, source, uploadingCallback)

		then: "The file uploading should be completed with resource does not exist error"
		1 * uploadingCallback.onError(_) >> {error = it[0]}
		error.context == Constants.CONTEXT
		error.errorCode == ResourceDoesNotExistException.ERROR_CODE
		error.severity == Severity.BUSINESS
	}

	def "Scenario: file uploading will be completed with resource does not exist error if resource id misses"() {
		UnspecifiedResourceException error;
		when: "The file uploading is requested for missing resource id"
		fileUploader.uploadFile(MISSING_RESOURCE_ID, source, uploadingCallback)

		then: "The file uploading should be completed with resource does not exist error"
		1 * uploadingCallback.onError(_) >> {error = it[0]}
		error.context == Constants.CONTEXT
		error.errorCode == UnspecifiedResourceException.ERROR_CODE
		error.severity == Severity.BUSINESS
	}
	
	private def createResource() {
		return Resource.builder()
				.id(RESOURCE_ID)
				.fileName(FILE_NAME)
				.build()
	}
}
