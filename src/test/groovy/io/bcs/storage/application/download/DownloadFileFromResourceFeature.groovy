package io.bcs.storage.application.download

import java.time.Instant
import java.time.temporal.ChronoUnit

import io.bcs.common.domain.model.io.transfer.CompletionCallback
import io.bcs.common.domain.model.io.transfer.DestinationPoint
import io.bcs.common.domain.model.io.transfer.SourcePoint
import io.bcs.common.domain.model.io.transfer.TransferingScheduler
import io.bcs.common.domain.model.io.transfer.Transmitter
import io.bcs.storage.application.download.FileDownloadService
import io.bcs.storage.domain.model.File
import io.bcs.storage.domain.model.FileRepository
import io.bcs.storage.domain.model.FileState
import io.bcs.storage.domain.model.FilesystemAccessor
import io.bcs.storage.domain.model.contracts.FileDescriptor
import io.bcs.storage.domain.model.contracts.FilePointer
import io.bcs.storage.domain.model.contracts.download.DownloadListener
import io.bcs.storage.domain.model.contracts.download.FileDownloader
import io.bcs.storage.domain.model.contracts.download.Fragment
import io.bcs.storage.domain.model.contracts.download.Range
import io.bcs.storage.domain.model.contracts.download.DownloadListener.DownloadProcessType
import io.bcs.storage.domain.model.contracts.download.FileDownloader.DownloadRequestDetails
import io.bcs.storage.domain.model.contracts.download.FileDownloader.FileDownloadRequest
import io.bcs.storage.domain.model.errors.FileDoesNotExistException
import io.bcs.storage.domain.model.states.DistributionState
import spock.lang.Specification

class DownloadFileFromResourceFeature extends Specification {
//	private static final Long RESOURCE_ID = 1L
	private static final String FILE_NAME = "file.txt"
	private static final String FILE_MEDIA_TYPE = "application/media"
	private static final String FILE_DISPOSITION = "inline"
//	private static final Optional<Long> EXISTING_RESOURCE_ID = Optional.of(RESOURCE_ID)
//	private static final Optional<Long> MISSING_RESOURCE_ID = Optional.empty()
	private static final String FILESYSTEM_NAME = "12345"
	private static final Instant CREATION_MOMENT = Instant.now()
	private static final Instant LAST_MODIFICATION = CREATION_MOMENT.plus(1, ChronoUnit.MINUTES)
	private static final Long FILE_SIZE = 100L

	private SourcePoint source
	private DestinationPoint destination
	private FileRepository fileRepository
	private FilesystemAccessor filesystemAccessor
	private TransferingScheduler transferringScheduler
	private FileDownloader fileDownloader


	def setup() {
		this.source = Stub(SourcePoint)
		this.destination = Stub(DestinationPoint)
		this.filesystemAccessor = Stub(FilesystemAccessor)
		this.fileRepository = Mock(FileRepository)
		this.transferringScheduler = Stub(TransferingScheduler)
		this.fileDownloader = new FileDownloadService(fileRepository, filesystemAccessor, transferringScheduler);
	}

//	def "Scenario: request file download for unspecified resource id"() {
//		UnspecifiedResourceException error;
//		given: "The file download request with unspecified resource id"
//		FilePointer revisionPointer = createFilePointer(Optional.empty(), Optional.empty())
//		FileDownloadRequest downloadRequest = createDownloadRequest(revisionPointer, [])
//
//		and: "The file download listener"
//		DownloadListener downloadListener = Mock(DownloadListener)
//
//		when: "The file download is requested"
//		fileDownloader.downloadFile(downloadRequest, downloadListener)
//
//		then: "The unspecified resource exception should be passed to the onRequestError of download the file download listener"
//		1 * downloadListener.onRequestError(downloadRequest, _) >> {error = it[1]}
//		error.getContext() == UnspecifiedResourceException.CONTEXT
//		error.getErrorCode() == UnspecifiedResourceException.ERROR_CODE
//	}

//	def "Scenario: request file range download for unspecified resource id"() {
//		UnspecifiedResourceException error;
//		given: "The file download request with unspecified resource id"
//		FilePointer revisionPointer = createFilePointer(Optional.empty(), Optional.empty())
//		FileDownloadRequest downloadRequest = createDownloadRequest(revisionPointer, [createRange(0L, 1L), createRange(1L, 1L)])
//
//		and: "The file download listener"
//		DownloadListener downloadListener = Mock(DownloadListener)
//
//		when: "The file download is requested"
//		fileDownloader.downloadFile(downloadRequest, downloadListener)
//
//		then: "The unspecified resource exception should be passed to the onRequestError of download the file download listener"
//		1 * downloadListener.onRequestError(downloadRequest, _) >> {error = it[1]}
//		error.getContext() == UnspecifiedResourceException.CONTEXT
//		error.getErrorCode() == UnspecifiedResourceException.ERROR_CODE
//	}
	
//	def "Scenario: request file download for unknown resource"() {
//		FileDoesNotExistException error;
//		given: "The file download request"
//		FilePointer revisionPointer = createFilePointer(Optional.of(RESOURCE_ID), Optional.empty())
//		FileDownloadRequest downloadRequest = createDownloadRequest(revisionPointer, [])
//
//		and: "The file download listener"
//		DownloadListener downloadListener = Mock(DownloadListener)
//
////		and: "There are uploads for specified resource"
//		fileRepository.findLatestResourceUpload(RESOURCE_ID) >> Optional.empty()
//
//		when: "The file download is requested"
//		fileDownloader.downloadFile(downloadRequest, downloadListener)
//
//		then: "The unspecified resource exception should be passed to the onRequestError of download the file download listener"
//		1 * downloadListener.onRequestError(downloadRequest, _) >> {error = it[1]}
//		error.getContext() == FileDoesNotExistException.CONTEXT
//		error.getErrorCode() == FileDoesNotExistException.ERROR_CODE
//	}
	
	
//	def "Scenario: request file ranges download for unknown resource"() {
//		FileDoesNotExistException error;
//		given: "The file download request"
//		FilePointer revisionPointer = createFilePointer(Optional.of(RESOURCE_ID), Optional.empty())
//		FileDownloadRequest downloadRequest = createDownloadRequest(revisionPointer, [createRange(0L, 1L)])
//
//		and: "The file download listener"
//		DownloadListener downloadListener = Mock(DownloadListener)
//
//		and: "There are uploads for specified resource"
//		fileRepository.findLatestResourceUpload(RESOURCE_ID) >> Optional.empty()
//
//		when: "The file download is requested"
//		fileDownloader.downloadFile(downloadRequest, downloadListener)
//
//		then: "The unspecified resource exception should be passed to the onRequestError of download the file download listener"
//		1 * downloadListener.onRequestError(downloadRequest, _) >> {error = it[1]}
//		error.getContext() == FileDoesNotExistException.CONTEXT
//		error.getErrorCode() == FileDoesNotExistException.ERROR_CODE
//	}
//	

//	def "Scenario: request file download with unspecified filesystem name"() {
//		given: "The file download request"
//		FilePointer revisionPointer = createFilePointer(Optional.of(RESOURCE_ID), Optional.empty())
//		FileDownloadRequest downloadRequest = createDownloadRequest(revisionPointer, [])
//
//		and: "The file download listener"
//		DownloadListener downloadListener = Mock(DownloadListener)
//
//		and: "The specified file doesn't exist in the repository"
//		fileRepository.findById(FILESYSTEM_NAME) >> Optional.empty()
//
//		and: "There are uploads for specified resource"
//		fileRepository.findLatestResourceUpload(RESOURCE_ID) >> Optional.of(createFile(new DistributionState(), FILE_SIZE))
//
//		and: "The file content transferring is completed successfully"
//		initSuccessfulTransferring();
//
//		when: "The file download is requested"
//		fileDownloader.downloadFile(downloadRequest, downloadListener)
//
//		then: "The download process should be started"
//		1 * downloadListener.onDownloadStart(DownloadProcessType.FULL_SIZE, _)
//		
//		and: "The download process should be completed"
//		1 * downloadListener.onDownloadComplete(_, FILE_SIZE)
//	}

//	def "Scenario: request single file range download with unspecified filesystem name"() {
//		FileDescriptor fileDescriptor;
//		Fragment fragment;
//		given: "The file download request"
//		FilePointer revisionPointer = createFilePointer(Optional.of(RESOURCE_ID), Optional.empty())
//		FileDownloadRequest downloadRequest = createDownloadRequest(revisionPointer, [createRange(0L, 1L)])
//
//		and: "The file download listener"
//		DownloadListener downloadListener = Mock(DownloadListener)
//
//		and: "The specified file doesn't exist in the repository"
//		fileRepository.findById(FILESYSTEM_NAME) >> Optional.empty()
//
//		and: "There are uploads for specified resource"
//		fileRepository.findLatestResourceUpload(RESOURCE_ID) >> Optional.of(createFile(new DistributionState(), FILE_SIZE))
//
//		and: "The file content transferring is completed successfully"
//		initSuccessfulTransferring();
//
//		when: "The file download is requested"
//		fileDownloader.downloadFile(downloadRequest, downloadListener)
//
//		then: "The download process should be started"
//		1 * downloadListener.onDownloadStart(DownloadProcessType.FULL_SIZE, _)
//
//		and: "The download process should be completed"
//		1 * downloadListener.onDownloadComplete(_, 2L) >> {fileDescriptor = it[0]}
//	}
	
//	def "Scenario: request multiple ranges download with unspecified filesystem name"() {
//		Fragment firstFragment;
//		Fragment secondFragment;
//		FileDescriptor fileDescriptor;
//		
//		given: "The file download request"
//		FilePointer revisionPointer = createFilePointer(Optional.of(RESOURCE_ID), Optional.empty())
//		FileDownloadRequest downloadRequest = createDownloadRequest(revisionPointer, [createRange(0L, 1L), createRange(1L, 1L)])
//
//		and: "The file download listener"
//		DownloadListener downloadListener = Mock(DownloadListener)
//
//		and: "The specified file doesn't exist in the repository"
//		fileRepository.findById(FILESYSTEM_NAME) >> Optional.empty()
//
//		and: "There are uploads for specified resource"
//		fileRepository.findLatestResourceUpload(RESOURCE_ID) >> Optional.of(createFile(new DistributionState(), FILE_SIZE))
//
//		and: "The file content transferring is completed successfully"
//		initSuccessfulTransferring();
//
//		when: "The file download is requested"
//		fileDownloader.downloadFile(downloadRequest, downloadListener)
//
//		then: "The download process should be started"
//		1 * downloadListener.onDownloadStart(DownloadProcessType.PARTIAL, _) >> {fileDescriptor = it[1]}
//		
//		and: "The fragments download should be started and completed for both fragments"
//		2 * downloadListener.onFragmentDownloadStart( _, _) >> {firstFragment = it[1]} >> {secondFragment = it[1]} 
//		2 * downloadListener.onFragmentDownloadComplete( _, _)
//		firstFragment.getSize() == 2L
//		secondFragment.getSize() == 1L
//		
//		and: "The download process should be completed"
//		1 * downloadListener.onDownloadComplete(_, 3L)
//	}
//	
	
	def "Scenario: request file download for unknown file"() {
		FileDoesNotExistException error;
		given: "The file download request"
		FilePointer revisionPointer = createFilePointer(Optional.of(FILESYSTEM_NAME))
		FileDownloadRequest downloadRequest = createDownloadRequest(revisionPointer, [])

		and: "The file download listener"
		DownloadListener downloadListener = Mock(DownloadListener)

		and: "The specified file doesn't exist in the repository"
		fileRepository.findById(FILESYSTEM_NAME) >> Optional.empty()

		when: "The file download is requested"
		fileDownloader.downloadFile(downloadRequest, downloadListener)

		then: "The unspecified resource exception should be passed to the onRequestError of download the file download listener"
		1 * downloadListener.onRequestError(downloadRequest, _) >> {error = it[1]}
		error.getContextId() == FileDoesNotExistException.CONTEXT
		error.getErrorCode() == FileDoesNotExistException.ERROR_CODE
	}
	
	
	def "Scenario: request file ranges download for unknown file"() {
		FileDoesNotExistException error;
		given: "The file download request"
		FilePointer revisionPointer = createFilePointer(Optional.of(FILESYSTEM_NAME))
		FileDownloadRequest downloadRequest = createDownloadRequest(revisionPointer, [createRange(0L, 1L)])

		and: "The file download listener"
		DownloadListener downloadListener = Mock(DownloadListener)

		and: "The specified file doesn't exist in the repository"
		fileRepository.findById(FILESYSTEM_NAME) >> Optional.empty()

		when: "The file download is requested"
		fileDownloader.downloadFile(downloadRequest, downloadListener)

		then: "The unspecified resource exception should be passed to the onRequestError of download the file download listener"
		1 * downloadListener.onRequestError(downloadRequest, _) >> {error = it[1]}
		error.getContextId() == FileDoesNotExistException.CONTEXT
		error.getErrorCode() == FileDoesNotExistException.ERROR_CODE
	}
	
	def "Scenario: request file download for specified file"() {
		given: "The file download request"
		FilePointer revisionPointer = createFilePointer(Optional.of(FILESYSTEM_NAME))
		FileDownloadRequest downloadRequest = createDownloadRequest(revisionPointer, [])

		and: "The file download listener"
		DownloadListener downloadListener = Mock(DownloadListener)

		and: "The specified file doesn't exist in the repository"
		fileRepository.findById(FILESYSTEM_NAME)>> Optional.of(createFile(new DistributionState(), FILE_SIZE))

		and: "The file content transferring is completed successfully"
		initSuccessfulTransferring();

		when: "The file download is requested"
		fileDownloader.downloadFile(downloadRequest, downloadListener)

		then: "The download process should be started"
		1 * downloadListener.onDownloadStart(DownloadProcessType.FULL_SIZE, _)
		
		and: "The download process should be completed"
		1 * downloadListener.onDownloadComplete(_, FILE_SIZE)
	}

	def "Scenario: request single file range download for specified file"() {
		FileDescriptor fileDescriptor;
		Fragment fragment;
		given: "The file download request"
		FilePointer revisionPointer = createFilePointer(Optional.of(FILESYSTEM_NAME))
		FileDownloadRequest downloadRequest = createDownloadRequest(revisionPointer, [createRange(0L, 1L)])

		and: "The file download listener"
		DownloadListener downloadListener = Mock(DownloadListener)

		and: "The specified file doesn't exist in the repository"
		fileRepository.findById(FILESYSTEM_NAME)>> Optional.of(createFile(new DistributionState(), FILE_SIZE))

		and: "The file content transferring is completed successfully"
		initSuccessfulTransferring();

		when: "The file download is requested"
		fileDownloader.downloadFile(downloadRequest, downloadListener)

		then: "The download process should be started"
		1 * downloadListener.onDownloadStart(DownloadProcessType.FULL_SIZE, _)

		and: "The download process should be completed"
		1 * downloadListener.onDownloadComplete(_, 2L) >> {fileDescriptor = it[0]}
	}
	
	def "Scenario: request multiple ranges download for specified file"() {
		Fragment firstFragment;
		Fragment secondFragment;
		FileDescriptor fileDescriptor;
		
		given: "The file download request"
		FilePointer revisionPointer = createFilePointer(Optional.of(FILESYSTEM_NAME))
		FileDownloadRequest downloadRequest = createDownloadRequest(revisionPointer, [createRange(0L, 1L), createRange(1L, 1L)])

		and: "The file download listener"
		DownloadListener downloadListener = Mock(DownloadListener)

		and: "The specified file doesn't exist in the repository"
		fileRepository.findById(FILESYSTEM_NAME)>> Optional.of(createFile(new DistributionState(), FILE_SIZE))
		
		and: "The file content transferring is completed successfully"
		initSuccessfulTransferring();

		when: "The file download is requested"
		fileDownloader.downloadFile(downloadRequest, downloadListener)

		then: "The download process should be started"
		1 * downloadListener.onDownloadStart(DownloadProcessType.PARTIAL, _) >> {fileDescriptor = it[1]}
		
		and: "The fragments download should be started and completed for both fragments"
		2 * downloadListener.onFragmentDownloadStart( _, _) >> {firstFragment = it[1]} >> {secondFragment = it[1]}
		2 * downloadListener.onFragmentDownloadComplete( _, _)
		firstFragment.getSize() == 2L
		secondFragment.getSize() == 1L
		
		and: "The download process should be completed"
		1 * downloadListener.onDownloadComplete(_, 3L)
	}
	
	def "Scenario: error during file download process"() {
		Exception error
		given: "The file download request"
		FilePointer revisionPointer = createFilePointer(Optional.of(FILESYSTEM_NAME))
		FileDownloadRequest downloadRequest = createDownloadRequest(revisionPointer, [])

		and: "The file download listener"
		DownloadListener downloadListener = Mock(DownloadListener)

		and: "The specified file doesn't exist in the repository"
		fileRepository.findById(FILESYSTEM_NAME)>> Optional.of(createFile(new DistributionState(), FILE_SIZE))

		and: "The file content transferring is completed successfully"
		initErrorTransferring(error)

		when: "The file download is requested"
		fileDownloader.downloadFile(downloadRequest, downloadListener)

		then: "The download process should be started"
		1 * downloadListener.onDownloadStart(DownloadProcessType.FULL_SIZE, _)
		
		and: "The download process should be failed"
		1 * downloadListener.onDownloadError(_, error);
	}
	
	def "Scenario: error during file ranges download process"() {
		Exception error
		given: "The file download request"
		FilePointer revisionPointer = createFilePointer(Optional.of(FILESYSTEM_NAME))
		FileDownloadRequest downloadRequest = createDownloadRequest(revisionPointer, [createRange(0L, 1L), createRange(1L, 1L)])

		and: "The file download listener"
		DownloadListener downloadListener = Mock(DownloadListener)

		and: "The specified file doesn't exist in the repository"
		fileRepository.findById(FILESYSTEM_NAME)>> Optional.of(createFile(new DistributionState(), FILE_SIZE))

		and: "The file content transferring is completed successfully"
		initErrorTransferring(error)

		when: "The file download is requested"
		fileDownloader.downloadFile(downloadRequest, downloadListener)

		then: "The download process should be started"
		1 * downloadListener.onDownloadStart(DownloadProcessType.PARTIAL, _)
		
		and: "Only first file fragment download should be started "
		1 * downloadListener.onFragmentDownloadStart(_, _)
		
		and: "Nothing fragments should be completed"
		0 * downloadListener.onFragmentDownloadComplete(_, _)
		
		and: "The download process should be failed"
		1 * downloadListener.onDownloadError(_, error);
	}
	
	def "Scenario: something went wrong during download process"() {
		RuntimeException error = new RuntimeException()
		given: "The file download request"
		FilePointer revisionPointer = createFilePointer(Optional.of(FILESYSTEM_NAME))
		FileDownloadRequest downloadRequest = createDownloadRequest(revisionPointer, [createRange(0L, 1L), createRange(1L, 1L)])

		and: "The file download listener"
		DownloadListener downloadListener = Mock(DownloadListener)

		and: "The specified file doesn't exist in the repository"
		fileRepository.findById(FILESYSTEM_NAME)>> Optional.of(createFile(new DistributionState(), FILE_SIZE))

		and: "Suddenly, the download listener throws an exception"
		downloadListener.onDownloadStart(_, _) >> {throw error}
		
		when: "The file download is requested"
		fileDownloader.downloadFile(downloadRequest, downloadListener)

		then: "The download process should be failed"
		1 * downloadListener.onDownloadError(_, error);
	}

	private FileDownloadRequest createDownloadRequest(FilePointer revisionPointer, Collection<Range> ranges) {
		FileDownloadRequest fileDownloadContext = Stub(FileDownloadRequest)
		DownloadRequestDetails downloadRequestDetails = Stub(DownloadRequestDetails)
		fileDownloadContext.getFile() >> revisionPointer
		fileDownloadContext.getDestinationPoint() >> destination
		fileDownloadContext.getRequestDetails() >> downloadRequestDetails
		downloadRequestDetails.getRanges() >> ranges
		return fileDownloadContext
	}

	private Range createRange(Long start, Long end) {
		Range range = Stub(Range)
		range.getStart() >> Optional.ofNullable(start)
		range.getEnd() >> Optional.ofNullable(end)
		return range
	}

	private void initSuccessfulTransferring() {
		transferringScheduler.schedule(_, _, _) >> { arguments ->
			CompletionCallback callback = arguments[2]
			return Stub(Transmitter) {
				start() >> {
					callback.onSuccess()
				}
			}
		}
	}

	private void initErrorTransferring(Exception error) {
		transferringScheduler.schedule(_, _, _) >> { arguments ->
			CompletionCallback callback = arguments[2]
			return Stub(Transmitter) {
				start() >> {
					callback.onError(error)
				}
			}
		}
	}

	private FilePointer createFilePointer(Optional<String> filesystemName) {
		FilePointer filePointer = Stub(FilePointer)
		filePointer.getFilesystemName() >> filesystemName
		return filePointer
	}

	private File createFile(FileState fileState, Long fileSize) {
		return File.builder()
				.filesystemName(FILESYSTEM_NAME)
				.fileName(FILE_NAME)
				.mediaType(FILE_MEDIA_TYPE)
				.contentDisposition(FILE_DISPOSITION)
				.creationMoment(CREATION_MOMENT)
				.lastModification(LAST_MODIFICATION)
				.state(fileState)
				.fileSize(fileSize)
				.build()
	}
}
