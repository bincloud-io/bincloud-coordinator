package io.bincloud.resources.application

import java.time.Instant
import java.time.temporal.ChronoUnit

import io.bincloud.common.domain.model.io.transfer.CompletionCallback
import io.bincloud.common.domain.model.io.transfer.DestinationPoint
import io.bincloud.files.domain.model.FileDescriptor
import io.bincloud.files.domain.model.contracts.FileStorage
import io.bincloud.resources.domain.model.Constants
import io.bincloud.resources.domain.model.ResourceRepository
import io.bincloud.resources.domain.model.contracts.FileDownloader
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

class DownloadFileFromResourceFeature extends Specification {
	private static final Long RESOURCE_ID = 1L
	private static final String FILE_ID = "12345"
	private static final Long FILE_SIZE = 10000L

	private ResourceRepository resourceRepository;
	private FileUploadsHistory fileUploadHistory;
	private FileStorage fileStorage;
	private FileDownloader fileDownloader;

	def setup() {
		this.resourceRepository = Mock(ResourceRepository)
		this.fileUploadHistory = Mock(FileUploadsHistory)
		this.fileStorage = Mock(FileStorage)
		this.fileDownloader = new FileDownloadService(resourceRepository, fileUploadHistory, fileStorage);
	}

	def "Scenario: download file with unspecified resource id"() {
		UnspecifiedResourceException error;
		given: "File download context with unspecified resource"
		FileDownloadContext fileDownloadContext = Stub(FileDownloadContext)
		initMockFileDownloadContext(fileDownloadContext, Optional.empty(), Optional.empty())

		and: "The download callback"
		DownloadCallback downloadCallback = Mock(DownloadCallback)

		when: "The file download requests"
		fileDownloader.downloadFile(fileDownloadContext, downloadCallback)

		then: "The unspecified resource exception should be thrown"
		1 * downloadCallback.onError(_) >> {error = it[0]}
		error.getContext() == Constants.CONTEXT
		error.getErrorCode() == UnspecifiedResourceException.ERROR_CODE
	}

	def "Scenario: download with specified resource id, but unspecified file id and missing resource in the repository"() {
		ResourceDoesNotExistException error;
		given: "File download context with specified resource and unspecified file id"
		FileDownloadContext fileDownloadContext = Stub(FileDownloadContext)
		initMockFileDownloadContext(fileDownloadContext, Optional.of(RESOURCE_ID), Optional.empty())

		and: "The resource doesn't exist in the repository"
		resourceRepository.isExists(RESOURCE_ID) >> false

		and: "The download callback"
		DownloadCallback downloadCallback = Mock(DownloadCallback)

		when: "The file download requests"
		fileDownloader.downloadFile(fileDownloadContext, downloadCallback)

		then: "The resource doesn't exist exception should be thrown"
		downloadCallback.onError(_) >> {error = it[0]}
		error.getContext() == Constants.CONTEXT
		error.getErrorCode() == ResourceDoesNotExistException.ERROR_CODE
	}


	def "Scenario: download with specified resource id and file id, but missing resource in the repository"() {
		ResourceDoesNotExistException error;
		given: "File download context with specified resource and file id"
		FileDownloadContext fileDownloadContext = Stub(FileDownloadContext)
		initMockFileDownloadContext(fileDownloadContext, Optional.of(RESOURCE_ID), Optional.of(FILE_ID))

		and: "The resource doesn't exist in the repository"
		resourceRepository.isExists(RESOURCE_ID) >> false

		and: "The download callback"
		DownloadCallback downloadCallback = Mock(DownloadCallback)

		when: "The file download requests"
		fileDownloader.downloadFile(fileDownloadContext, downloadCallback)

		then: "The resource doesn't exist exception should be thrown"
		downloadCallback.onError(_) >> {error = it[0]}
		error.getContext() == Constants.CONTEXT
		error.getErrorCode() == ResourceDoesNotExistException.ERROR_CODE
	}

	def "Scenario: download with specified resource id, but unspecified file id and missing uploads in uploads history"() {
		ResourceDoesNotHaveUploadsException error;
		given: "File download context with specified resource and unspecified file id"
		FileDownloadContext fileDownloadContext = Stub(FileDownloadContext)
		initMockFileDownloadContext(fileDownloadContext, Optional.of(RESOURCE_ID), Optional.empty())

		and: "The resource exists in the repository"
		resourceRepository.isExists(RESOURCE_ID) >> true

		and: "The file history contains nothing uploads"
		fileUploadHistory.findFileUploadForResource(RESOURCE_ID) >> Optional.empty()

		and: "The download callback"
		DownloadCallback downloadCallback = Mock(DownloadCallback)

		when: "The file download requests"
		fileDownloader.downloadFile(fileDownloadContext, downloadCallback)

		then: "The resource doesn't have uploads exception should be thrown"
		downloadCallback.onError(_) >> {error = it[0]}
		error.getContext() == Constants.CONTEXT
		error.getErrorCode() == ResourceDoesNotHaveUploadsException.ERROR_CODE
	}


	def "Scenario: download with specified resource id and file id, but missing upload in uploads history"() {
		ResourceDoesNotHaveUploadsException error;
		given: "File download context with specified resource and file id"
		FileDownloadContext fileDownloadContext = Stub(FileDownloadContext)
		initMockFileDownloadContext(fileDownloadContext, Optional.of(RESOURCE_ID), Optional.of(FILE_ID))

		and: "The resource exists in the repository"
		resourceRepository.isExists(RESOURCE_ID) >> true

		and: "The file history doesn't contain upload"
		fileUploadHistory.checkFileUploadExistence(RESOURCE_ID, FILE_ID) >> false

		and: "The download callback"
		DownloadCallback downloadCallback = Mock(DownloadCallback)

		when: "The file download requests"
		fileDownloader.downloadFile(fileDownloadContext, downloadCallback)

		then: "The resource doesn't have uploads exception should be thrown"
		downloadCallback.onError(_) >> {error = it[0]}
		error.getContext() == Constants.CONTEXT
		error.getErrorCode() == ResourceDoesNotHaveUploadsException.ERROR_CODE
	}

	def "Scenario: download with specified resource id, unspecified file id and unknown descriptor"() {
		UploadedFileDescriptorHasNotBeenFoundException error;
		given: "File download context with specified resource and unspecified file id"
		FileDownloadContext fileDownloadContext = Stub(FileDownloadContext)
		initMockFileDownloadContext(fileDownloadContext, Optional.of(RESOURCE_ID), Optional.empty())

		and: "The resource exists in the store"
		resourceRepository.isExists(RESOURCE_ID) >> true

		and: "The file history contains upload"
		fileUploadHistory.findFileUploadForResource(RESOURCE_ID) >> Optional.of(createFileUpload())

		and: "The download callback"
		DownloadCallback downloadCallback = Mock(DownloadCallback)

		and: "The file descriptor doestn't exist in the storage"
		fileStorage.getFileDescriptor(FILE_ID) >> Optional.empty()

		when: "The file download requests"
		fileDownloader.downloadFile(fileDownloadContext, downloadCallback)

		then: "The resource doesn't have uploads exception should be thrown"
		downloadCallback.onError(_) >> {error = it[0]}
		error.getContext() == Constants.CONTEXT
		error.getErrorCode() == UploadedFileDescriptorHasNotBeenFoundException.ERROR_CODE
	}

	def "Scenario: download with specified resource id,file id and unknown descriptor"() {
		UploadedFileDescriptorHasNotBeenFoundException error;
		given: "File download context with specified resource and file id"
		FileDownloadContext fileDownloadContext = Stub(FileDownloadContext)
		initMockFileDownloadContext(fileDownloadContext, Optional.of(RESOURCE_ID), Optional.of(FILE_ID))

		and: "The resource exists in the store"
		resourceRepository.isExists(RESOURCE_ID) >> true

		and: "The file history contains upload"
		fileUploadHistory.checkFileUploadExistence(RESOURCE_ID, FILE_ID) >> true

		and: "The download callback"
		DownloadCallback downloadCallback = Mock(DownloadCallback)

		and: "The file descriptor doestn't exist in the storage"
		fileStorage.getFileDescriptor(FILE_ID) >> Optional.empty()

		when: "The file download requests"
		fileDownloader.downloadFile(fileDownloadContext, downloadCallback)

		then: "The resource doesn't have uploads exception should be thrown"
		downloadCallback.onError(_) >> {error = it[0]}
		error.getContext() == Constants.CONTEXT
		error.getErrorCode() == UploadedFileDescriptorHasNotBeenFoundException.ERROR_CODE
	}


	def "Scenario: successfully download with specified resource id and unspecified file id"() {
		DownloadedFile downloadedFile;
		given: "File download context with specified resource and unspecified file id"
		FileDownloadContext fileDownloadContext = Stub(FileDownloadContext)
		initMockFileDownloadContext(fileDownloadContext, Optional.of(RESOURCE_ID), Optional.empty())

		and: "The resource exists in the store"
		resourceRepository.isExists(RESOURCE_ID) >> true

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

		and: "File download process successfully completes"
		fileStorage.downloadFile(FILE_ID, _, _) >> {
			CompletionCallback callback = it[2]
			callback.onSuccess()
		}

		when: "The file download requests"
		fileDownloader.downloadFile(fileDownloadContext, downloadCallback)

		then: "The downloaded file information should be received"
		downloadCallback.onDownload(_) >> {downloadedFile = it[0]}
		downloadedFile.getFileId() == FILE_ID
		downloadedFile.getFileSize() == FILE_SIZE
	}

	def "Scenario: successfully download with specified resource id and file id"() {
		DownloadedFile downloadedFile;
		given: "File download context with specified resource and unspecified file id"
		FileDownloadContext fileDownloadContext = Stub(FileDownloadContext)
		initMockFileDownloadContext(fileDownloadContext, Optional.of(RESOURCE_ID), Optional.of(FILE_ID))

		and: "The resource exists in the store"
		resourceRepository.isExists(RESOURCE_ID) >> true

		and: "The file history contains upload"
		fileUploadHistory.checkFileUploadExistence(RESOURCE_ID, FILE_ID) >> true

		and: "The download callback"
		DownloadCallback downloadCallback = Mock(DownloadCallback)

		and: "The file descriptor exists in the storage"
		FileDescriptor descriptor = Stub(FileDescriptor)
		descriptor.getCreationMoment() >> Instant.now().minus(1, ChronoUnit.MINUTES)
		descriptor.getLastModification() >> Instant.now()
		descriptor.getSize() >> FILE_SIZE
		fileStorage.getFileDescriptor(FILE_ID) >> Optional.of(descriptor)

		and: "File download process successfully completes"
		fileStorage.downloadFile(FILE_ID, _, _) >> {
			CompletionCallback callback = it[2]
			callback.onSuccess()
		}

		when: "The file download requests"
		fileDownloader.downloadFile(fileDownloadContext, downloadCallback)

		then: "The downloaded file information should be received"
		downloadCallback.onDownload(_) >> {downloadedFile = it[0]}
		downloadedFile.getFileId() == FILE_ID
		downloadedFile.getFileSize() == FILE_SIZE
	}

	def "Scenario: failed download for specified resource id and unspecified file id"() {
		Exception error;
		given: "File download context with specified resource and unspecified file id"
		FileDownloadContext fileDownloadContext = Stub(FileDownloadContext)
		initMockFileDownloadContext(fileDownloadContext, Optional.of(RESOURCE_ID), Optional.empty())

		and: "The resource exists in the store"
		resourceRepository.isExists(RESOURCE_ID) >> true

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

		and: "File download process completes with error"
		fileStorage.downloadFile(FILE_ID, _, _) >> {
			CompletionCallback callback = it[2]
			callback.onError(new Exception("DOWNLOAD_ERROR"))
		}

		when: "The file download requests"
		fileDownloader.downloadFile(fileDownloadContext, downloadCallback)

		then: "The error should be received by download callback"
		downloadCallback.onError(_) >> {error = it[0]}
		error.getMessage() == "DOWNLOAD_ERROR"
	}

	def "Scenario: failed download for specified resource id and file id"() {
		Exception error;
		given: "File download context with specified resource and unspecified file id"
		FileDownloadContext fileDownloadContext = Stub(FileDownloadContext)
		initMockFileDownloadContext(fileDownloadContext, Optional.of(RESOURCE_ID), Optional.of(FILE_ID))

		and: "The resource exists in the store"
		resourceRepository.isExists(RESOURCE_ID) >> true

		and: "The file history contains upload"
		fileUploadHistory.checkFileUploadExistence(RESOURCE_ID, FILE_ID) >> true

		and: "The download callback"
		DownloadCallback downloadCallback = Mock(DownloadCallback)

		and: "The file descriptor exists in the storage"
		FileDescriptor descriptor = Stub(FileDescriptor)
		descriptor.getCreationMoment() >> Instant.now().minus(1, ChronoUnit.MINUTES)
		descriptor.getLastModification() >> Instant.now()
		descriptor.getSize() >> FILE_SIZE
		fileStorage.getFileDescriptor(FILE_ID) >> Optional.of(descriptor)

		and: "File download process completes with error"
		fileStorage.downloadFile(FILE_ID, _, _) >> {
			CompletionCallback callback = it[2]
			callback.onError(new Exception("DOWNLOAD_ERROR"))
		}

		when: "The file download requests"
		fileDownloader.downloadFile(fileDownloadContext, downloadCallback)

		then: "The error should be received by download callback"
		downloadCallback.onError(_) >> {error = it[0]}
		error.getMessage() == "DOWNLOAD_ERROR"
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
		mockDownloadContext.getDestinationPoint() >> Stub(DestinationPoint)
	}
}
