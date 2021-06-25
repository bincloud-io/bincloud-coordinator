package io.bincloud.resources.application

import io.bincloud.files.domain.model.contracts.FileStorage
import io.bincloud.resources.domain.model.ResourceRepository
import io.bincloud.resources.domain.model.contracts.FileDownloader
import io.bincloud.resources.domain.model.file.FileUploadsHistory
import spock.lang.Specification

class DownloadFileFromResourceFeature extends Specification {
	private static final Long RESOURCE_ID = 1L
	private static final String FILE_ID = "12345"
	private static final Long FILE_SIZE = 10000L
	private static final Long FILE_RANGE_START = 100L
	private static final Long FILE_RANGE_END = 200L
	private static final Long FILE_RANGE_SIZE = 100L
	private static final String FILE_NAME = "filename.txt"
	
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

//
//	def "Scenario: successfully download with specified resource id and unspecified file id"() {
//		DownloadedFile downloadedFile;
//		given: "File download context with specified resource and unspecified file id"
//		FileDownloadContext fileDownloadContext = Stub(FileDownloadContext)
//		initMockFileDownloadContext(fileDownloadContext, Optional.of(RESOURCE_ID), Optional.empty())
//
//		and: "The resource exists in the store"
//		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())
//
//		and: "The file history contains nothing uploads"
//		fileUploadHistory.findFileUploadForResource(RESOURCE_ID) >> Optional.of(createFileUpload())
//
//		and: "The download callback"
//		DownloadVisitor downloadCallback = Mock(DownloadVisitor)
//
//		and: "The file descriptor exists in the storage"
//		FileDescriptor descriptor = Stub(FileDescriptor)
//		descriptor.getCreationMoment() >> Instant.now().minus(1, ChronoUnit.MINUTES)
//		descriptor.getLastModification() >> Instant.now()
//		descriptor.getSize() >> FILE_SIZE
//		fileStorage.getFileDescriptor(FILE_ID) >> Optional.of(descriptor)
//
//		and: "File download process successfully completes"
//		fileStorage.downloadFile(FILE_ID, _, _) >> {
//			CompletionCallback callback = it[2]
//			callback.onSuccess()
//		}
//
//		when: "The file download requests"
//		fileDownloader.downloadFile(fileDownloadContext, downloadCallback).downloadFile()
//
//		then: "The downloaded file information should be received"
//		downloadCallback.onDownload(_) >> {downloadedFile = it[0]}
//		downloadedFile.getFileId() == FILE_ID
//		downloadedFile.getTransferredContentLength() == FILE_SIZE
//		downloadedFile.getFileName() == FILE_NAME
//	}
//	
//	def "Scenario: successfully download range with specified resource id and unspecified file id"() {
//		DownloadedFile downloadedFile;
//		given: "File download context with specified resource and unspecified file id"
//		FileRangeDownloadContext fileDownloadContext = Stub(FileRangeDownloadContext)
//		initMockFileDownloadContext(fileDownloadContext, Optional.of(RESOURCE_ID), Optional.empty())
//		initMockFileRange(fileDownloadContext)
//
//		and: "The resource exists in the store"
//		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())
//
//		and: "The file history contains nothing uploads"
//		fileUploadHistory.findFileUploadForResource(RESOURCE_ID) >> Optional.of(createFileUpload())
//
//		and: "The download callback"
//		DownloadVisitor downloadCallback = Mock(DownloadVisitor)
//
//		and: "The file descriptor exists in the storage"
//		FileDescriptor descriptor = Stub(FileDescriptor)
//		descriptor.getCreationMoment() >> Instant.now().minus(1, ChronoUnit.MINUTES)
//		descriptor.getLastModification() >> Instant.now()
//		descriptor.getSize() >> FILE_SIZE
//		fileStorage.getFileDescriptor(FILE_ID) >> Optional.of(descriptor)
//
//		and: "File download process successfully completes"
//		fileStorage.downloadFileRange(FILE_ID, _, _, _, _) >> {
//			CompletionCallback callback = it[2]
//			callback.onSuccess()
//		}
//
//		when: "The file download requests"
//		fileDownloader.downloadFileRange(fileDownloadContext, downloadCallback).downloadFile()
//
//		then: "The downloaded file information should be received"
//		downloadCallback.onDownload(_) >> {downloadedFile = it[0]}
//		downloadedFile.getFileId() == FILE_ID
//		downloadedFile.getTransferredContentLength() == FILE_RANGE_SIZE
//		downloadedFile.getFileName() == FILE_NAME
//	}
//
//	def "Scenario: successfully download with specified resource id and file id"() {
//		DownloadedFile downloadedFile;
//		given: "File download context with specified resource and unspecified file id"
//		FileDownloadContext fileDownloadContext = Stub(FileDownloadContext)
//		initMockFileDownloadContext(fileDownloadContext, Optional.of(RESOURCE_ID), Optional.of(FILE_ID))
//
//		and: "The resource exists in the store"
//		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())
//
//		and: "The file history contains upload"
//		fileUploadHistory.checkFileUploadExistence(RESOURCE_ID, FILE_ID) >> true
//
//		and: "The download callback"
//		DownloadVisitor downloadCallback = Mock(DownloadVisitor)
//
//		and: "The file descriptor exists in the storage"
//		FileDescriptor descriptor = Stub(FileDescriptor)
//		descriptor.getCreationMoment() >> Instant.now().minus(1, ChronoUnit.MINUTES)
//		descriptor.getLastModification() >> Instant.now()
//		descriptor.getSize() >> FILE_SIZE
//		fileStorage.getFileDescriptor(FILE_ID) >> Optional.of(descriptor)
//
//		and: "File download process successfully completes"
//		fileStorage.downloadFile(FILE_ID, _, _) >> {
//			CompletionCallback callback = it[2]
//			callback.onSuccess()
//		}
//
//		when: "The file download requests"
//		fileDownloader.downloadFile(fileDownloadContext, downloadCallback).downloadFile()
//
//		then: "The downloaded file information should be received"
//		downloadCallback.onDownload(_) >> {downloadedFile = it[0]}
//		downloadedFile.getFileId() == FILE_ID
//		downloadedFile.getTransferredContentLength() == FILE_SIZE
//		downloadedFile.getFileName() == FILE_NAME
//	}
//
//	def "Scenario: broken download for specified resource id and unspecified file id"() {
//		Exception error;
//		given: "File download context with specified resource and unspecified file id"
//		FileDownloadContext fileDownloadContext = Stub(FileDownloadContext)
//		initMockFileDownloadContext(fileDownloadContext, Optional.of(RESOURCE_ID), Optional.empty())
//
//		and: "The resource exists in the store"
//		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())
//
//		and: "The file history contains nothing uploads"
//		fileUploadHistory.findFileUploadForResource(RESOURCE_ID) >> Optional.of(createFileUpload())
//
//		and: "The download callback"
//		DownloadVisitor downloadCallback = Mock(DownloadVisitor)
//
//		and: "The file descriptor exists in the storage"
//		FileDescriptor descriptor = Stub(FileDescriptor)
//		descriptor.getCreationMoment() >> Instant.now().minus(1, ChronoUnit.MINUTES)
//		descriptor.getLastModification() >> Instant.now()
//		descriptor.getSize() >> FILE_SIZE
//		fileStorage.getFileDescriptor(FILE_ID) >> Optional.of(descriptor)
//
//		and: "File download process completes with error"
//		fileStorage.downloadFile(FILE_ID, _, _) >> {
//			CompletionCallback callback = it[2]
//			callback.onError(new Exception("DOWNLOAD_ERROR"))
//		}
//
//		when: "The file download requests"
//		fileDownloader.downloadFile(fileDownloadContext, downloadCallback).downloadFile()
//
//		then: "The error should be received by download callback"
//		downloadCallback.onError(_) >> {error = it[0]}
//		error.getMessage() == "DOWNLOAD_ERROR"
//	}
//
//	def "Scenario: broken download for specified resource id and file id"() {
//		Exception error;
//		given: "File download context with specified resource and unspecified file id"
//		FileDownloadContext fileDownloadContext = Stub(FileDownloadContext)
//		initMockFileDownloadContext(fileDownloadContext, Optional.of(RESOURCE_ID), Optional.of(FILE_ID))
//
//		and: "The resource exists in the store"
//		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())
//
//		and: "The file history contains upload"
//		fileUploadHistory.checkFileUploadExistence(RESOURCE_ID, FILE_ID) >> true
//
//		and: "The download callback"
//		DownloadVisitor downloadCallback = Mock(DownloadVisitor)
//
//		and: "The file descriptor exists in the storage"
//		FileDescriptor descriptor = Stub(FileDescriptor)
//		descriptor.getCreationMoment() >> Instant.now().minus(1, ChronoUnit.MINUTES)
//		descriptor.getLastModification() >> Instant.now()
//		descriptor.getSize() >> FILE_SIZE
//		fileStorage.getFileDescriptor(FILE_ID) >> Optional.of(descriptor)
//
//		and: "File download process completes with error"
//		fileStorage.downloadFile(FILE_ID, _, _) >> {
//			CompletionCallback callback = it[2]
//			callback.onError(new Exception("DOWNLOAD_ERROR"))
//		}
//
//		when: "The file download requests"
//		fileDownloader.downloadFile(fileDownloadContext, downloadCallback).downloadFile()
//
//		then: "The error should be received by download callback"
//		downloadCallback.onError(_) >> {error = it[0]}
//		error.getMessage() == "DOWNLOAD_ERROR"
//	}
//
//	private Resource createResource() {
//		return Resource.builder()
//				.id(RESOURCE_ID)
//				.fileName(FILE_NAME)
//				.build();
//	}
//
//	private FileUpload createFileUpload() {
//		return FileUpload.builder()
//				.resourceId(RESOURCE_ID)
//				.fileId(FILE_ID)
//				.uploadMoment(Instant.now())
//				.build()
//	}
//
//	private void initMockFileDownloadContext(FileDownloadContext mockDownloadContext, Optional<Long> resourceId, Optional<String> fileId) {
//		mockDownloadContext.getResourceId() >> resourceId
//		mockDownloadContext.getFileId() >> fileId
//		mockDownloadContext.getDestinationPoint() >> Stub(DestinationPoint)
//	}
//	
//	private void initMockFileRange(FileRangeDownloadContext rangeDownloadContext) {
//		Range range = Stub(Range)
//		rangeDownloadContext.getRange() >> range
//		range.getEnd() >> Optional.of(FILE_RANGE_START)
//		range.getStart() >> Optional.of(FILE_RANGE_END)
//	}
//	
}
