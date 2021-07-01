package io.bincloud.resources.application.download

import java.time.Instant
import java.time.temporal.ChronoUnit

import io.bincloud.common.domain.model.error.ApplicationException.Severity
import io.bincloud.common.domain.model.io.transfer.CompletionCallback
import io.bincloud.common.domain.model.io.transfer.DestinationPoint
import io.bincloud.files.domain.model.FileDescriptor
import io.bincloud.files.domain.model.contracts.FileStorage
import io.bincloud.resources.domain.model.Constants
import io.bincloud.resources.domain.model.Resource
import io.bincloud.resources.domain.model.ResourceRepository
import io.bincloud.resources.domain.model.contracts.RevisionPointer
import io.bincloud.resources.domain.model.contracts.download.DownloadListener
import io.bincloud.resources.domain.model.contracts.download.FileDownloader
import io.bincloud.resources.domain.model.contracts.download.MultiRangeDownloadListener
import io.bincloud.resources.domain.model.contracts.download.Range
import io.bincloud.resources.domain.model.contracts.download.FileDownloader.FileDownloadRequest
import io.bincloud.resources.domain.model.contracts.download.Fragment
import io.bincloud.resources.domain.model.errors.ResourceDoesNotExistException
import io.bincloud.resources.domain.model.errors.ResourceDoesNotHaveUploadsException
import io.bincloud.resources.domain.model.errors.UnsatisfiableRangeFormatException
import io.bincloud.resources.domain.model.errors.UnspecifiedResourceException
import io.bincloud.resources.domain.model.errors.UploadedFileDescriptorHasNotBeenFoundException
import io.bincloud.resources.domain.model.file.FileUpload
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
	private static final Instant CREATION_MOMENT = Instant.now()
	private static final Instant LAST_MODIFICATION_MOMENT = CREATION_MOMENT.plus(1, ChronoUnit.DAYS);

	private ResourceRepository resourceRepository;
	private FileUploadsHistory fileUploadHistory;
	private FileStorage fileStorage;
	private FileDownloader fileDownloader;
	private DestinationPoint destinationPoint;

	def setup() {
		this.resourceRepository = Mock(ResourceRepository)
		this.fileUploadHistory = Mock(FileUploadsHistory)
		this.fileStorage = Mock(FileStorage)
		this.destinationPoint = Stub(DestinationPoint)
		this.fileDownloader = new FileDownloadService(resourceRepository, fileUploadHistory, fileStorage);
	}

	def "Scenario: download whole file for unspecified resource id"() {
		UnspecifiedResourceException error;
		given: "The file download request with unspecified resource id"
		RevisionPointer revisionPointer = createRevisionPointer(Optional.empty(), Optional.empty())
		FileDownloadRequest downloadRequest = createDownloadRequest(revisionPointer, [])

		and: "The file download listener"
		DownloadListener downloadListener = Mock(DownloadListener)

		when: "The file download is requested"
		fileDownloader.downloadFile(downloadRequest, downloadListener).downloadFile()

		then: "The unspecified resource exception should be passed to the onRequestError of download the file download listener"
		1 * downloadListener.onRequestError(downloadRequest, _) >> {error = it[1]}
		error.getContext() == Constants.CONTEXT
		error.getErrorCode() == UnspecifiedResourceException.ERROR_CODE
	}

	def "Scenario: download file range for unspecified resource id"() {
		UnspecifiedResourceException error;
		given: "The file download request with unspecified resource id"
		RevisionPointer revisionPointer = createRevisionPointer(Optional.empty(), Optional.empty())
		FileDownloadRequest downloadRequest = createDownloadRequest(revisionPointer, [createRange(0L, 1L)])

		and: "The file download listener"
		MultiRangeDownloadListener downloadListener = Mock(MultiRangeDownloadListener)

		when: "The file download is requested"
		fileDownloader.downloadFileRanges(downloadRequest, downloadListener).downloadFile()

		then: "The unspecified resource exception should be passed to the onRequestError of download the file download listener"
		1 * downloadListener.onRequestError(downloadRequest, _) >> {error = it[1]}
		error.getContext() == Constants.CONTEXT
		error.getErrorCode() == UnspecifiedResourceException.ERROR_CODE
	}

	def "Scenario: download file for missing resource"() {
		ResourceDoesNotExistException error;
		given: "The file download request"
		RevisionPointer revisionPointer = createRevisionPointer(Optional.of(RESOURCE_ID), Optional.empty())
		FileDownloadRequest downloadRequest = createDownloadRequest(revisionPointer, [])

		and: "The resource doesn't exist in the repository"
		resourceRepository.findById(RESOURCE_ID) >> Optional.empty()

		and: "The file download listener"
		DownloadListener downloadListener = Mock(DownloadListener)

		when: "The file download is requested"
		fileDownloader.downloadFile(downloadRequest, downloadListener).downloadFile()

		then: "The resource doesn't exist exception should be thrown"
		1 * downloadListener.onRequestError(downloadRequest, _) >> {error = it[1]}
		error.getContext() == Constants.CONTEXT
		error.getErrorCode() == ResourceDoesNotExistException.ERROR_CODE
	}

	def "Scenario: download file ranges for missing resource"() {
		ResourceDoesNotExistException error;
		given: "The file download request"
		RevisionPointer revisionPointer = createRevisionPointer(Optional.of(RESOURCE_ID), Optional.empty())
		FileDownloadRequest downloadRequest = createDownloadRequest(revisionPointer, [createRange(0L, 1L)])

		and: "The resource doesn't exist in the repository"
		resourceRepository.findById(RESOURCE_ID) >> Optional.empty()

		and: "The file download listener"
		MultiRangeDownloadListener downloadListener = Mock(MultiRangeDownloadListener)

		when: "The file download is requested"
		fileDownloader.downloadFileRanges(downloadRequest, downloadListener).downloadFile()

		then: "The resource doesn't exist exception should be thrown"
		1 * downloadListener.onRequestError(downloadRequest, _) >> {error = it[1]}
		error.getContext() == Constants.CONTEXT
		error.getErrorCode() == ResourceDoesNotExistException.ERROR_CODE
	}

	def "Scenario: download latest file revision of missing uploads in history for this resource"() {
		ResourceDoesNotHaveUploadsException error;
		given: "The file download request"
		RevisionPointer revisionPointer = createRevisionPointer(Optional.of(RESOURCE_ID), Optional.empty())
		FileDownloadRequest downloadRequest = createDownloadRequest(revisionPointer, [])

		and: "The resource exists in the repository"
		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())

		and: "The file history contains nothing uploads"
		fileUploadHistory.findFileUploadForResource(RESOURCE_ID) >> Optional.empty()

		and: "The file download listener"
		DownloadListener downloadListener = Mock(DownloadListener)

		when: "The file download is requested"
		fileDownloader.downloadFile(downloadRequest, downloadListener).downloadFile()


		then: "The resource doesn't have uploads exception should be thrown"
		1 * downloadListener.onRequestError(downloadRequest, _) >> {error = it[1]}
		error.getContext() == Constants.CONTEXT
		error.getErrorCode() == ResourceDoesNotHaveUploadsException.ERROR_CODE
	}

	def "Scenario: download latest file revision ranges of missing uploads in history for this resource"() {
		ResourceDoesNotHaveUploadsException error;
		given: "The file download request"
		RevisionPointer revisionPointer = createRevisionPointer(Optional.of(RESOURCE_ID), Optional.empty())
		FileDownloadRequest downloadRequest = createDownloadRequest(revisionPointer, [createRange(0L, 1L)])

		and: "The resource exists in the repository"
		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())

		and: "The file history contains nothing uploads"
		fileUploadHistory.findFileUploadForResource(RESOURCE_ID) >> Optional.empty()

		and: "The file download listener"
		MultiRangeDownloadListener downloadListener = Mock(MultiRangeDownloadListener)

		when: "The file download is requested"
		fileDownloader.downloadFileRanges(downloadRequest, downloadListener).downloadFile()


		then: "The resource doesn't have uploads exception should be thrown"
		1 * downloadListener.onRequestError(downloadRequest, _) >> {error = it[1]}
		error.getContext() == Constants.CONTEXT
		error.getErrorCode() == ResourceDoesNotHaveUploadsException.ERROR_CODE
	}

	def "Scenario: download specified file revision of missing uploads in history for this resource"() {
		ResourceDoesNotHaveUploadsException error;
		given: "The file download request"
		RevisionPointer revisionPointer = createRevisionPointer(Optional.of(RESOURCE_ID), Optional.of(FILE_ID))
		FileDownloadRequest downloadRequest = createDownloadRequest(revisionPointer, [])

		and: "The resource exists in the repository"
		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())

		and: "The file history doesn't contain upload"
		fileUploadHistory.checkFileUploadExistence(RESOURCE_ID, FILE_ID) >> false

		and: "The file download listener"
		DownloadListener downloadListener = Mock(DownloadListener)

		when: "The file download is requested"
		fileDownloader.downloadFile(downloadRequest, downloadListener).downloadFile()


		then: "The resource doesn't have uploads exception should be thrown"
		1 * downloadListener.onRequestError(downloadRequest, _) >> {error = it[1]}
		error.getContext() == Constants.CONTEXT
		error.getErrorCode() == ResourceDoesNotHaveUploadsException.ERROR_CODE
	}

	def "Scenario: download specified file revision ranges of missing uploads in history for this resource"() {
		ResourceDoesNotHaveUploadsException error;
		given: "The file download request"
		RevisionPointer revisionPointer = createRevisionPointer(Optional.of(RESOURCE_ID), Optional.of(FILE_ID))
		FileDownloadRequest downloadRequest = createDownloadRequest(revisionPointer, [createRange(0L, 1L)])

		and: "The resource exists in the repository"
		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())

		and: "The file history doesn't contain upload"
		fileUploadHistory.checkFileUploadExistence(RESOURCE_ID, FILE_ID) >> false

		and: "The file download listener"
		MultiRangeDownloadListener downloadListener = Mock(MultiRangeDownloadListener)

		when: "The file download is requested"
		fileDownloader.downloadFileRanges(downloadRequest, downloadListener).downloadFile()

		then: "The resource doesn't have uploads exception should be thrown"
		1 * downloadListener.onRequestError(downloadRequest, _) >> {error = it[1]}
		error.getContext() == Constants.CONTEXT
		error.getErrorCode() == ResourceDoesNotHaveUploadsException.ERROR_CODE
	}

	def "Scenario: download latest file revision when file descriptor isn't found in the store"() {
		UploadedFileDescriptorHasNotBeenFoundException error;
		given: "The file download request"
		RevisionPointer revisionPointer = createRevisionPointer(Optional.of(RESOURCE_ID), Optional.empty())
		FileDownloadRequest downloadRequest = createDownloadRequest(revisionPointer, [])

		and: "The resource exists in the store"
		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())

		and: "The file history contains upload"
		fileUploadHistory.findFileUploadForResource(RESOURCE_ID) >> Optional.of(createFileUpload())

		and: "The file descriptor doestn't exist in the storage"
		fileStorage.getFileDescriptor(FILE_ID) >> Optional.empty()

		and: "The file download listener"
		DownloadListener downloadListener = Mock(DownloadListener)

		when: "The file download is requested"
		fileDownloader.downloadFile(downloadRequest, downloadListener).downloadFile()

		then: "The resource doesn't have uploads exception should be thrown"
		1 * downloadListener.onRequestError(downloadRequest, _) >> {error = it[1]}
		error.getContext() == Constants.CONTEXT
		error.getErrorCode() == UploadedFileDescriptorHasNotBeenFoundException.ERROR_CODE
	}

	def "Scenario: download latest file revision ranges when file descriptor isn't found in the store"() {
		UploadedFileDescriptorHasNotBeenFoundException error;
		given: "The file download request"
		RevisionPointer revisionPointer = createRevisionPointer(Optional.of(RESOURCE_ID), Optional.empty())
		FileDownloadRequest downloadRequest = createDownloadRequest(revisionPointer, [createRange(0L, 1L)])

		and: "The resource exists in the store"
		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())

		and: "The file history contains upload"
		fileUploadHistory.findFileUploadForResource(RESOURCE_ID) >> Optional.of(createFileUpload())

		and: "The file descriptor doestn't exist in the storage"
		fileStorage.getFileDescriptor(FILE_ID) >> Optional.empty()

		and: "The file download listener"
		MultiRangeDownloadListener downloadListener = Mock(MultiRangeDownloadListener)

		when: "The file download is requested"
		fileDownloader.downloadFileRanges(downloadRequest, downloadListener).downloadFile()

		then: "The resource doesn't have uploads exception should be thrown"
		1 * downloadListener.onRequestError(downloadRequest, _) >> {error = it[1]}
		error.getContext() == Constants.CONTEXT
		error.getErrorCode() == UploadedFileDescriptorHasNotBeenFoundException.ERROR_CODE
	}
	
	def "Scenario: download specified file revision when file descriptor isn't found in the store"() {
		UploadedFileDescriptorHasNotBeenFoundException error;
		given: "The file download request"
		RevisionPointer revisionPointer = createRevisionPointer(Optional.of(RESOURCE_ID), Optional.of(FILE_ID))
		FileDownloadRequest downloadRequest = createDownloadRequest(revisionPointer, [])

		and: "The resource exists in the store"
		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())

		and: "The file history contains upload"
		fileUploadHistory.checkFileUploadExistence(RESOURCE_ID, FILE_ID) >> true

		and: "The file descriptor doestn't exist in the storage"
		fileStorage.getFileDescriptor(FILE_ID) >> Optional.empty()

		and: "The file download listener"
		DownloadListener downloadListener = Mock(DownloadListener)

		when: "The file download is requested"
		fileDownloader.downloadFile(downloadRequest, downloadListener).downloadFile()

		then: "The resource doesn't have uploads exception should be thrown"
		1 * downloadListener.onRequestError(downloadRequest, _) >> {error = it[1]}
		error.getContext() == Constants.CONTEXT
		error.getErrorCode() == UploadedFileDescriptorHasNotBeenFoundException.ERROR_CODE
	}

	def "Scenario: download specified file revision ranges when file descriptor isn't found in the store"() {
		UploadedFileDescriptorHasNotBeenFoundException error;
		given: "The file download request"
		RevisionPointer revisionPointer = createRevisionPointer(Optional.of(RESOURCE_ID), Optional.of(FILE_ID))
		FileDownloadRequest downloadRequest = createDownloadRequest(revisionPointer, [createRange(0L, 1L)])

		and: "The resource exists in the store"
		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())

		and: "The file history contains upload"
		fileUploadHistory.checkFileUploadExistence(RESOURCE_ID, FILE_ID) >> true

		and: "The file descriptor doestn't exist in the storage"
		fileStorage.getFileDescriptor(FILE_ID) >> Optional.empty()

		and: "The file download listener"
		MultiRangeDownloadListener downloadListener = Mock(MultiRangeDownloadListener)

		when: "The file download is requested"
		fileDownloader.downloadFileRanges(downloadRequest, downloadListener).downloadFile()

		then: "The resource doesn't have uploads exception should be thrown"
		1 * downloadListener.onRequestError(downloadRequest, _) >> {error = it[1]}
		error.getContext() == Constants.CONTEXT
		error.getErrorCode() == UploadedFileDescriptorHasNotBeenFoundException.ERROR_CODE
	}
	
	def "Scenario: download empty range collection"() {
		UnsatisfiableRangeFormatException exception;
		given: "The file download request"
		RevisionPointer revisionPointer = createRevisionPointer(Optional.of(RESOURCE_ID), Optional.of(FILE_ID))
		FileDownloadRequest downloadRequest = createDownloadRequest(revisionPointer, [])

		and: "The resource exists in the store"
		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())

		and: "The file history contains upload"
		fileUploadHistory.checkFileUploadExistence(RESOURCE_ID, FILE_ID) >> true

		and: "The file descriptor exists in the storage"
		fileStorage.getFileDescriptor(FILE_ID) >> createFileRescriptor()

		and: "The file download listener"
		MultiRangeDownloadListener downloadListener = Mock(MultiRangeDownloadListener)

		when: "The file download is requested"
		fileDownloader.downloadFileRanges(downloadRequest, downloadListener).downloadFile()

		then: "The download visitor should be notified about start"
		1 * downloadListener.onDownloadStart(_)

		and: "The download visitor should be notified about error"
		1 * downloadListener.onDownloadError(_, _) >> {
			exception = it[1]
		}

		and: "The download visitor shouldn't be notified about end of process"
		0 * downloadListener.onDownloadComplete(_)

		and: "The unsatisfiable range format exception should be received"
		exception.getContext() == Constants.CONTEXT
		exception.getErrorCode() == UnsatisfiableRangeFormatException.ERROR_CODE
		exception.getSeverity() == Severity.BUSINESS
	}
	
	
	def "Scenario: error during ranges download process"() {
		Exception exception = new Exception();
		Fragment downloadedFragment;
		given: "The file download request"
		RevisionPointer revisionPointer = createRevisionPointer(Optional.of(RESOURCE_ID), Optional.of(FILE_ID))
		FileDownloadRequest downloadRequest = createDownloadRequest(revisionPointer, [
			createRange(0L, 0L),createRange(1L, 1L),createRange(2L, 2L)
		])

		and: "The resource exists in the store"
		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())

		and: "The file history contains upload"
		fileUploadHistory.checkFileUploadExistence(RESOURCE_ID, FILE_ID) >> true

		and: "The file descriptor exists in the storage"
		fileStorage.getFileDescriptor(FILE_ID) >> createFileRescriptor()

		and: "The file download listener"
		MultiRangeDownloadListener downloadListener = Mock(MultiRangeDownloadListener)

		and: "The first range download is completed successfully"
		fileStorage.downloadFileRange(FILE_ID, destinationPoint, _, 0L, 1L) >> {
			CompletionCallback callback = it[2]
			callback.onSuccess()
		}
		
		and: "The second range download is completed with failure"
		fileStorage.downloadFileRange(FILE_ID, destinationPoint, _, 1L, 1L) >> {
			CompletionCallback callback = it[2]
			callback.onError(exception)
		}
		
		when: "The file download is requested"
		fileDownloader.downloadFileRanges(downloadRequest, downloadListener).downloadFile()
		
		then: "The download process should be started"
		1 * downloadListener.onDownloadStart(_)
		
		and: "The first fragment should be successfully transferred"
		1 * downloadListener.onFragmentDownloadComplete(_, _) >> {
			downloadedFragment = it[1]
		}
		downloadedFragment.getStart() == 0L
		downloadedFragment.getSize() == 1L
		
		and: "The second fragment should be completed with failure"
		1 * downloadListener.onDownloadError(_, exception)
		
		and: "The process shouldn't be completed successfully"
		0 * downloadListener.onDownloadComplete(_)
		
		and: "The download process should be broken after first failure"
		0 * fileStorage.downloadFileRange(FILE_ID, destinationPoint, _, 2L, 1L)
	}
	
	
	def "Scenario: successfully downloaded ranges"() {
		Fragment firstFragment;
		Fragment secondFragment;
		Fragment thirdFragment;
		given: "The file download request"
		RevisionPointer revisionPointer = createRevisionPointer(Optional.of(RESOURCE_ID), Optional.of(FILE_ID))
		FileDownloadRequest downloadRequest = createDownloadRequest(revisionPointer, [
			createRange(0L, 0L),createRange(1L, 1L),createRange(2L, 2L)
		])

		and: "The resource exists in the store"
		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())

		and: "The file history contains upload"
		fileUploadHistory.checkFileUploadExistence(RESOURCE_ID, FILE_ID) >> true

		and: "The file descriptor exists in the storage"
		fileStorage.getFileDescriptor(FILE_ID) >> createFileRescriptor()

		and: "The file download listener"
		MultiRangeDownloadListener downloadListener = Mock(MultiRangeDownloadListener)

		and: "The ranges download is completed successfully"
		fileStorage.downloadFileRange(FILE_ID, destinationPoint, _, _, _) >> {
			CompletionCallback callback = it[2]
			callback.onSuccess()
		}
		
		when: "The file download is requested"
		fileDownloader.downloadFileRanges(downloadRequest, downloadListener).downloadFile()
		
		then: "The download process should be started"
		1 * downloadListener.onDownloadStart(_)
		
		and: "The first fragment should be successfully transferred"
		1 * downloadListener.onFragmentDownloadComplete(_, _) >> {
			firstFragment = it[1]
		}
		firstFragment.getStart() == 0L
		firstFragment.getSize() == 1L

		and: "The second fragment should be successfully transferred"
		1 * downloadListener.onFragmentDownloadComplete(_, _) >> {
			secondFragment = it[1]
		}
		secondFragment.getStart() == 1L
		secondFragment.getSize() == 1L
		
		and: "The third fragment should be successfully transferred"
		1 * downloadListener.onFragmentDownloadComplete(_, _) >> {
			thirdFragment = it[1]
		}
		thirdFragment.getStart() == 2L
		thirdFragment.getSize() == 1L
				
		and: "The second fragment shouldn't be completed with failure"
		0 * downloadListener.onDownloadError(_, _)
		
		and: "The process should be completed successfully"
		1 * downloadListener.onDownloadComplete(_)		
	}
	
	def "Scenario: download whole file successfully"() {
		given: "The file download request"
		RevisionPointer revisionPointer = createRevisionPointer(Optional.of(RESOURCE_ID), Optional.of(FILE_ID))
		FileDownloadRequest downloadRequest = createDownloadRequest(revisionPointer, [])

		and: "The resource exists in the store"
		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())

		and: "The file history contains upload"
		fileUploadHistory.checkFileUploadExistence(RESOURCE_ID, FILE_ID) >> true

		and: "The file descriptor exists in the storage"
		fileStorage.getFileDescriptor(FILE_ID) >> createFileRescriptor()

		and: "The file download listener"
		DownloadListener downloadListener = Mock(DownloadListener)

		and: "The ranges download is completed successfully"
		fileStorage.downloadFile(FILE_ID, destinationPoint, _) >> {
			CompletionCallback callback = it[2]
			callback.onSuccess()
		}
		
		when: "The file download is requested"
		fileDownloader.downloadFile(downloadRequest, downloadListener).downloadFile()
		
		then: "The download process should be started"
		1 * downloadListener.onDownloadStart(_)
		
						
		and: "The second fragment shouldn't be completed with failure"
		0 * downloadListener.onDownloadError(_, _)
		
		and: "The process should be completed successfully"
		1 * downloadListener.onDownloadComplete(_)
	}
	
	def "Scenario: download whole file with error"() {
		Exception error = new Exception()
		given: "The file download request"
		RevisionPointer revisionPointer = createRevisionPointer(Optional.of(RESOURCE_ID), Optional.of(FILE_ID))
		FileDownloadRequest downloadRequest = createDownloadRequest(revisionPointer, [])

		and: "The resource exists in the store"
		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())

		and: "The file history contains upload"
		fileUploadHistory.checkFileUploadExistence(RESOURCE_ID, FILE_ID) >> true

		and: "The file descriptor exists in the storage"
		fileStorage.getFileDescriptor(FILE_ID) >> createFileRescriptor()

		and: "The file download listener"
		DownloadListener downloadListener = Mock(DownloadListener)

		and: "The ranges download is completed successfully"
		fileStorage.downloadFile(FILE_ID, destinationPoint, _) >> {
			CompletionCallback callback = it[2]
			callback.onError(error)
		}
		
		when: "The file download is requested"
		fileDownloader.downloadFile(downloadRequest, downloadListener).downloadFile()
		
		then: "The download process should be started"
		1 * downloadListener.onDownloadStart(_)
		
						
		and: "The second fragment should be completed with failure"
		1 * downloadListener.onDownloadError(_, _)
		
		and: "The process shouldn't be completed successfully"
		0 * downloadListener.onDownloadComplete(_)
	}

	private FileDownloadRequest createDownloadRequest(RevisionPointer revisionPointer, Collection<Range> ranges) {
		FileDownloadRequest fileDownloadContext = Stub(FileDownloadRequest)
		fileDownloadContext.getDestinationPoint() >> destinationPoint
		fileDownloadContext.getRanges() >> ranges
		fileDownloadContext.getRevision() >> revisionPointer
		return fileDownloadContext
	}
	
	private Range createRange(Long start, Long end) {
		Range range = Stub(Range)
		range.getStart() >> Optional.ofNullable(start)
		range.getEnd() >> Optional.ofNullable(end)
		return range
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
				.uploadMoment(LAST_MODIFICATION_MOMENT)
				.build()
	}
	
	private Optional<FileDescriptor> createFileRescriptor() {
		FileDescriptor fileDescriptor = Stub(FileDescriptor)
		fileDescriptor.getStatus() >> "DISTRIBUTION"
		fileDescriptor.getCreationMoment() >> CREATION_MOMENT
		fileDescriptor.getLastModification() >> LAST_MODIFICATION_MOMENT
		fileDescriptor.getSize() >> FILE_SIZE
		return Optional.of(fileDescriptor)
	}

	private RevisionPointer createRevisionPointer(Optional<Long> resourceId, Optional<String> fileId) {
		RevisionPointer revisionPointer = Stub(RevisionPointer)
		revisionPointer.getResourceId() >> resourceId
		revisionPointer.getFileId() >> fileId
		return revisionPointer
	}
}
