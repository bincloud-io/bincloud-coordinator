package io.bincloud.resources.application.download

import java.time.Instant
import java.time.temporal.ChronoUnit

import io.bincloud.common.domain.model.io.transfer.DestinationPoint
import io.bincloud.files.domain.model.FileDescriptor
import io.bincloud.files.domain.model.contracts.FileStorage
import io.bincloud.resources.application.download.operations.DownoadOperationContext
import io.bincloud.resources.domain.model.Constants
import io.bincloud.resources.domain.model.Resource
import io.bincloud.resources.domain.model.ResourceRepository
import io.bincloud.resources.domain.model.contracts.FileDownloader.DownloadCallback
import io.bincloud.resources.domain.model.contracts.FileDownloader.DownloadedFile
import io.bincloud.resources.domain.model.contracts.FileDownloader.FileDownloadContext
import io.bincloud.resources.domain.model.errors.ResourceDoesNotExistException
import io.bincloud.resources.domain.model.errors.ResourceDoesNotHaveUploadsException
import io.bincloud.resources.domain.model.errors.UnspecifiedResourceException
import io.bincloud.resources.domain.model.errors.UploadedFileDescriptorHasNotBeenFoundException
import io.bincloud.resources.domain.model.file.FileUpload
import io.bincloud.resources.domain.model.file.FileUploadsHistory
import spock.lang.Specification

class DownloadOperationContextSpec extends Specification {
	private static final Long RESOURCE_ID = 1L
	private static final String FILE_ID = "12345"
	private static final Long FILE_SIZE = 10000L
	private static final String FILE_NAME = "filename.txt"

	private ResourceRepository resourceRepository;
	private FileUploadsHistory fileUploadHistory;
	private FileStorage fileStorage;
	private DestinationPoint destintationPoint;

	def setup() {
		this.resourceRepository = Mock(ResourceRepository)
		this.fileUploadHistory = Mock(FileUploadsHistory)
		this.destintationPoint = Stub(DestinationPoint)
		this.fileStorage = Mock(FileStorage)
	}

	def "Scenario: create download operation context for unspecified resource id"() {
		UnspecifiedResourceException error;
		given: "File download context with unspecified resource"
		FileDownloadContext fileDownloadContext = Stub(FileDownloadContext)
		initMockFileDownloadContext(fileDownloadContext, Optional.empty(), Optional.empty())

		and: "The download callback"
		DownloadCallback downloadCallback = Mock(DownloadCallback)

		when: "The download operation context creates"
		new DownoadOperationContext(fileDownloadContext, downloadCallback, fileStorage, fileUploadHistory, resourceRepository)

		then: "The unspecified resource exception should be thrown"
		error = thrown()
		error.getContext() == Constants.CONTEXT
		error.getErrorCode() == UnspecifiedResourceException.ERROR_CODE
	}

	def "Scenario: create download operation context for specified resource id, but unspecified file id and missing resource in the repository"() {
		ResourceDoesNotExistException error;
		given: "File download context with specified resource and unspecified file id"
		FileDownloadContext fileDownloadContext = Stub(FileDownloadContext)
		initMockFileDownloadContext(fileDownloadContext, Optional.of(RESOURCE_ID), Optional.empty())

		and: "The resource doesn't exist in the repository"
		resourceRepository.findById(RESOURCE_ID) >> Optional.empty()

		and: "The download callback"
		DownloadCallback downloadCallback = Mock(DownloadCallback)

		when: "The download operation context creates"
		new DownoadOperationContext(fileDownloadContext, downloadCallback, fileStorage, fileUploadHistory, resourceRepository)

		then: "The resource doesn't exist exception should be thrown"
		error = thrown()
		error.getContext() == Constants.CONTEXT
		error.getErrorCode() == ResourceDoesNotExistException.ERROR_CODE
	}

	def "Scenario: create download operation context for specified resource id and file id, but missing resource in the repository"() {
		ResourceDoesNotExistException error;
		given: "File download context with specified resource and file id"
		FileDownloadContext fileDownloadContext = Stub(FileDownloadContext)
		initMockFileDownloadContext(fileDownloadContext, Optional.of(RESOURCE_ID), Optional.of(FILE_ID))

		and: "The resource doesn't exist in the repository"
		resourceRepository.findById(RESOURCE_ID) >> Optional.empty()

		and: "The download callback"
		DownloadCallback downloadCallback = Mock(DownloadCallback)

		when: "The download operation context creates"
		new DownoadOperationContext(fileDownloadContext, downloadCallback, fileStorage, fileUploadHistory, resourceRepository)

		then: "The resource doesn't exist exception should be thrown"
		error = thrown()
		error.getContext() == Constants.CONTEXT
		error.getErrorCode() == ResourceDoesNotExistException.ERROR_CODE
	}

	def "Scenario: create download operation context for specified resource id, but unspecified file id and missing uploads in uploads history"() {
		ResourceDoesNotHaveUploadsException error;
		given: "File download context with specified resource and unspecified file id"
		FileDownloadContext fileDownloadContext = Stub(FileDownloadContext)
		initMockFileDownloadContext(fileDownloadContext, Optional.of(RESOURCE_ID), Optional.empty())

		and: "The resource exists in the repository"
		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())

		and: "The file history contains nothing uploads"
		fileUploadHistory.findFileUploadForResource(RESOURCE_ID) >> Optional.empty()

		and: "The download callback"
		DownloadCallback downloadCallback = Mock(DownloadCallback)

		when: "The download operation context creates"
		new DownoadOperationContext(fileDownloadContext, downloadCallback, fileStorage, fileUploadHistory, resourceRepository)

		then: "The resource doesn't have uploads exception should be thrown"
		error = thrown()
		error.getContext() == Constants.CONTEXT
		error.getErrorCode() == ResourceDoesNotHaveUploadsException.ERROR_CODE
	}

	def "Scenario: create download operation context for specified resource id and file id, but missing upload in uploads history"() {
		ResourceDoesNotHaveUploadsException error;
		given: "File download context with specified resource and file id"
		FileDownloadContext fileDownloadContext = Stub(FileDownloadContext)
		initMockFileDownloadContext(fileDownloadContext, Optional.of(RESOURCE_ID), Optional.of(FILE_ID))

		and: "The resource exists in the repository"
		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())

		and: "The file history doesn't contain upload"
		fileUploadHistory.checkFileUploadExistence(RESOURCE_ID, FILE_ID) >> false

		and: "The download callback"
		DownloadCallback downloadCallback = Mock(DownloadCallback)

		when: "The download operation context creates"
		new DownoadOperationContext(fileDownloadContext, downloadCallback, fileStorage, fileUploadHistory, resourceRepository)

		then: "The resource doesn't have uploads exception should be thrown"
		error = thrown()
		error.getContext() == Constants.CONTEXT
		error.getErrorCode() == ResourceDoesNotHaveUploadsException.ERROR_CODE
	}

	def "Scenario: create download operation context for specified resource id, unspecified file id and unknown descriptor"() {
		UploadedFileDescriptorHasNotBeenFoundException error;
		given: "File download context with specified resource and unspecified file id"
		FileDownloadContext fileDownloadContext = Stub(FileDownloadContext)
		initMockFileDownloadContext(fileDownloadContext, Optional.of(RESOURCE_ID), Optional.empty())

		and: "The resource exists in the store"
		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())

		and: "The file history contains upload"
		fileUploadHistory.findFileUploadForResource(RESOURCE_ID) >> Optional.of(createFileUpload())

		and: "The download callback"
		DownloadCallback downloadCallback = Mock(DownloadCallback)

		and: "The file descriptor doestn't exist in the storage"
		fileStorage.getFileDescriptor(FILE_ID) >> Optional.empty()

		when: "The download operation context creates"
		new DownoadOperationContext(fileDownloadContext, downloadCallback, fileStorage, fileUploadHistory, resourceRepository)

		then: "The resource doesn't have uploads exception should be thrown"
		error = thrown()
		error.getContext() == Constants.CONTEXT
		error.getErrorCode() == UploadedFileDescriptorHasNotBeenFoundException.ERROR_CODE
	}

	def "Scenario: create download operation context for specified resource id,file id and unknown descriptor"() {
		UploadedFileDescriptorHasNotBeenFoundException error;
		given: "File download context with specified resource and file id"
		FileDownloadContext fileDownloadContext = Stub(FileDownloadContext)
		initMockFileDownloadContext(fileDownloadContext, Optional.of(RESOURCE_ID), Optional.of(FILE_ID))

		and: "The resource exists in the store"
		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())

		and: "The file history contains upload"
		fileUploadHistory.checkFileUploadExistence(RESOURCE_ID, FILE_ID) >> true

		and: "The download callback"
		DownloadCallback downloadCallback = Mock(DownloadCallback)

		and: "The file descriptor doestn't exist in the storage"
		fileStorage.getFileDescriptor(FILE_ID) >> Optional.empty()

		when: "The download operation context creates"
		new DownoadOperationContext(fileDownloadContext, downloadCallback, fileStorage, fileUploadHistory, resourceRepository)

		then: "The resource doesn't have uploads exception should be thrown"
		error = thrown()
		error.getContext() == Constants.CONTEXT
		error.getErrorCode() == UploadedFileDescriptorHasNotBeenFoundException.ERROR_CODE
	}

	def "Scenario: download operation context successfully created"() {
		DownloadedFile downloadedFile;
		Exception error = new Exception("Something went wrong")
		given: "File download context with specified resource and unspecified file id"
		FileDownloadContext fileDownloadContext = Stub(FileDownloadContext)
		initMockFileDownloadContext(fileDownloadContext, Optional.of(RESOURCE_ID), Optional.empty())

		and: "The resource exists in the store"
		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())

		and: "The file history contains nothing uploads"
		fileUploadHistory.findFileUploadForResource(RESOURCE_ID) >> Optional.of(createFileUpload())

		and: "The download callback"
		DownloadCallback downloadCallback = Mock(DownloadCallback)

		and: "The file descriptor exists in the storage"
		FileDescriptor descriptor = Stub(FileDescriptor)
		descriptor.getCreationMoment() >> Instant.now().minus(1, ChronoUnit.MINUTES)
		descriptor.getLastModification() >> Instant.now()
		descriptor.getSize() >> FILE_SIZE
		fileStorage.getFileDescriptor(FILE_ID) >> Optional.of(descriptor)


		when: "The file download requests"
		DownoadOperationContext context = new DownoadOperationContext(fileDownloadContext, downloadCallback, fileStorage, fileUploadHistory, resourceRepository)
		
		and: "The success callback method calls"
		context.getDownloadCompletionCallback().onSuccess()
		
		and: "The error callback method calls" 
		context.getDownloadCompletionCallback().onError(error)
		
		then: "The download completion callback should pass downloaded file to download callback on success"
		1 * downloadCallback.onDownload(_) >> {downloadedFile = it[0]}
		context.getDownloadedFile().is(downloadedFile)
		
		and: "The download completion callback should pass errors on error to download callback"
		1 * downloadCallback.onError(error)
		
		and: "The destination point should be provided from context"
		context.getDestinationPoint().is(destintationPoint)
		
		and: "The file id should be provided"
		context.getFileId() == FILE_ID
	}

	private Resource createResource() {
		return Resource.builder()
				.id(RESOURCE_ID)
				.fileName(FILE_NAME)
				.build();
	}

	private FileUpload createFileUpload() {
		return FileUpload.builder()
				.resourceId(RESOURCE_ID)
				.fileId(FILE_ID)
				.uploadMoment(Instant.now())
				.build()
	}

	private void initMockFileDownloadContext(FileDownloadContext mockDownloadContext, Optional<Long> resourceId, Optional<String> fileId) {
		mockDownloadContext.getResourceId() >> resourceId
		mockDownloadContext.getFileId() >> fileId
		mockDownloadContext.getDestinationPoint() >> destintationPoint
	}
}
