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
import io.bincloud.resources.domain.model.contracts.Fragment
import io.bincloud.resources.domain.model.contracts.Range
import io.bincloud.resources.domain.model.contracts.download.MultiRangeDownloadVisitor
import io.bincloud.resources.domain.model.contracts.download.DownloadVisitor
import io.bincloud.resources.domain.model.contracts.download.FileRevisionDescriptor
import io.bincloud.resources.domain.model.contracts.download.RevisionPointer
import io.bincloud.resources.domain.model.contracts.download.FileDownloader.FileDownloadContext
import io.bincloud.resources.domain.model.errors.ResourceDoesNotExistException
import io.bincloud.resources.domain.model.errors.ResourceDoesNotHaveUploadsException
import io.bincloud.resources.domain.model.errors.UnsatisfiableRangeFormatException
import io.bincloud.resources.domain.model.errors.UnspecifiedResourceException
import io.bincloud.resources.domain.model.errors.UploadedFileDescriptorHasNotBeenFoundException
import io.bincloud.resources.domain.model.file.FileUpload
import io.bincloud.resources.domain.model.file.FileUploadsHistory
import spock.lang.Specification

class FileRevisionAccessorSpec extends Specification {
	private static final Long RESOURCE_ID = 1L
	private static final String FILE_ID = "12345"
	private static final Long FILE_SIZE = 10000L
	private static final String FILE_NAME = "filename.txt"
	private static final Instant CREATION_MOMENT = Instant.now()
	private static final Instant LAST_MODIFICATION_MOMENT = CREATION_MOMENT.plus(1, ChronoUnit.DAYS);

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

	def "Scenario: create file revision accessor for unspecified resource id"() {
		UnspecifiedResourceException error;
		given: "Unspecified resource revision pointer"
		RevisionPointer revisionPointer = createRevisionPointer(Optional.empty(), Optional.empty())

		when: "The file revision accessor creates"
		new FileRevisionAccessor(revisionPointer, fileStorage, fileUploadHistory, resourceRepository)

		then: "The unspecified resource exception should be thrown"
		error = thrown()
		error.getContext() == Constants.CONTEXT
		error.getErrorCode() == UnspecifiedResourceException.ERROR_CODE
	}

	def "Scenario: create file revision accessor for specified resource id, but unspecified file id and missing resource in the repository"() {
		ResourceDoesNotExistException error;
		given: "The revision pointer with unspecified file id"
		RevisionPointer revisionPointer = createRevisionPointer(Optional.of(RESOURCE_ID), Optional.empty())

		and: "The resource doesn't exist in the repository"
		resourceRepository.findById(RESOURCE_ID) >> Optional.empty()

		when: "The file revision accessor creates"
		new FileRevisionAccessor(revisionPointer, fileStorage, fileUploadHistory, resourceRepository)

		then: "The resource doesn't exist exception should be thrown"
		error = thrown()
		error.getContext() == Constants.CONTEXT
		error.getErrorCode() == ResourceDoesNotExistException.ERROR_CODE
	}

	def "Scenario: create file revision accessor for specified resource id and file id, but missing resource in the repository"() {
		ResourceDoesNotExistException error;
		given: "The revision pointer with specified file id"
		RevisionPointer revisionPointer = createRevisionPointer(Optional.of(RESOURCE_ID), Optional.of(FILE_ID))

		and: "The resource doesn't exist in the repository"
		resourceRepository.findById(RESOURCE_ID) >> Optional.empty()

		when: "The file revision accessor creates"
		new FileRevisionAccessor(revisionPointer, fileStorage, fileUploadHistory, resourceRepository)

		then: "The resource doesn't exist exception should be thrown"
		error = thrown()
		error.getContext() == Constants.CONTEXT
		error.getErrorCode() == ResourceDoesNotExistException.ERROR_CODE
	}

	def "Scenario: create file revision accessor for specified resource id, but unspecified file id and missing uploads in uploads history"() {
		ResourceDoesNotHaveUploadsException error;
		given: "The revision pointer with unspecified file id"
		RevisionPointer revisionPointer = createRevisionPointer(Optional.of(RESOURCE_ID), Optional.empty())

		and: "The resource exists in the repository"
		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())

		and: "The file history contains nothing uploads"
		fileUploadHistory.findFileUploadForResource(RESOURCE_ID) >> Optional.empty()

		when: "The file revision accessor creates"
		new FileRevisionAccessor(revisionPointer, fileStorage, fileUploadHistory, resourceRepository)

		then: "The resource doesn't have uploads exception should be thrown"
		error = thrown()
		error.getContext() == Constants.CONTEXT
		error.getErrorCode() == ResourceDoesNotHaveUploadsException.ERROR_CODE
	}

	def "Scenario: create file revision accessor for specified resource id and file id, but missing upload in uploads history"() {
		ResourceDoesNotHaveUploadsException error;
		given: "The revision pointer with specified file id"
		RevisionPointer revisionPointer = createRevisionPointer(Optional.of(RESOURCE_ID), Optional.of(FILE_ID))

		and: "The resource exists in the repository"
		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())

		and: "The file history doesn't contain upload"
		fileUploadHistory.checkFileUploadExistence(RESOURCE_ID, FILE_ID) >> false

		when: "The file revision accessor creates"
		new FileRevisionAccessor(revisionPointer, fileStorage, fileUploadHistory, resourceRepository)

		then: "The resource doesn't have uploads exception should be thrown"
		error = thrown()
		error.getContext() == Constants.CONTEXT
		error.getErrorCode() == ResourceDoesNotHaveUploadsException.ERROR_CODE
	}

	def "Scenario: create file revision accessor for specified resource id, unspecified file id and unknown descriptor"() {
		UploadedFileDescriptorHasNotBeenFoundException error;
		given: "The revision pointer with unspecified file id"
		RevisionPointer revisionPointer = createRevisionPointer(Optional.of(RESOURCE_ID), Optional.empty())

		and: "The resource exists in the store"
		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())

		and: "The file history contains upload"
		fileUploadHistory.findFileUploadForResource(RESOURCE_ID) >> Optional.of(createFileUpload())

		and: "The file descriptor doestn't exist in the storage"
		fileStorage.getFileDescriptor(FILE_ID) >> Optional.empty()

		when: "The file revision accessor creates"
		new FileRevisionAccessor(revisionPointer, fileStorage, fileUploadHistory, resourceRepository)

		then: "The resource doesn't have uploads exception should be thrown"
		error = thrown()
		error.getContext() == Constants.CONTEXT
		error.getErrorCode() == UploadedFileDescriptorHasNotBeenFoundException.ERROR_CODE
	}

	def "Scenario: create file revision accessor for specified resource id,file id and unknown descriptor"() {
		UploadedFileDescriptorHasNotBeenFoundException error;
		given: "The revision pointer with specified file id"
		RevisionPointer revisionPointer = createRevisionPointer(Optional.of(RESOURCE_ID), Optional.of(FILE_ID))

		and: "The resource exists in the store"
		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())

		and: "The file history contains upload"
		fileUploadHistory.checkFileUploadExistence(RESOURCE_ID, FILE_ID) >> true

		and: "The file descriptor doestn't exist in the storage"
		fileStorage.getFileDescriptor(FILE_ID) >> Optional.empty()

		when: "The file revision accessor creates"
		new FileRevisionAccessor(revisionPointer, fileStorage, fileUploadHistory, resourceRepository)

		then: "The resource doesn't have uploads exception should be thrown"
		error = thrown()
		error.getContext() == Constants.CONTEXT
		error.getErrorCode() == UploadedFileDescriptorHasNotBeenFoundException.ERROR_CODE
	}

	def "Scenario: get file revision descriptor by file revision accessor about specified file revision in history"() {
		given: "The revision pointer with specified file id"
		RevisionPointer revisionPointer = createRevisionPointer(Optional.of(RESOURCE_ID), Optional.of(FILE_ID))

		and: "The resource exists in the store"
		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())

		and: "The file history contains upload"
		fileUploadHistory.checkFileUploadExistence(RESOURCE_ID, FILE_ID) >> true

		and: "The file descriptor doestn't exist in the storage"
		fileStorage.getFileDescriptor(FILE_ID) >> createFileRescriptor()

		when: "The file revision accessor creates"
		FileRevisionAccessor fileRevisionAccessor = new FileRevisionAccessor(revisionPointer, fileStorage, fileUploadHistory, resourceRepository)
		FileRevisionDescriptor fileRevisionDescriptor = fileRevisionAccessor.getRevisionDescriptor()

		then: "The information about file revision should be successfully aggregated"
		fileRevisionDescriptor.getResourceId() == RESOURCE_ID
		fileRevisionDescriptor.getFileId() == FILE_ID
		fileRevisionDescriptor.getFileName() == FILE_NAME
		fileRevisionDescriptor.getFileSize() == FILE_SIZE
		fileRevisionDescriptor.getLastModification() == LAST_MODIFICATION_MOMENT
	}

	def "Scenario: get file revision by file revision accessor about latest file revision in history"() {
		given: "The file revision accessor"
		RevisionPointer revisionPointer = createRevisionPointer(Optional.of(RESOURCE_ID), Optional.empty())
		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())
		fileUploadHistory.findFileUploadForResource(RESOURCE_ID) >> Optional.of(createFileUpload())
		fileStorage.getFileDescriptor(FILE_ID) >> createFileRescriptor()
		FileRevisionAccessor fileRevisionAccessor = new FileRevisionAccessor(revisionPointer, fileStorage, fileUploadHistory, resourceRepository)

		when: "The revision descriptor is requested"
		FileRevisionDescriptor fileRevisionDescriptor = fileRevisionAccessor.getRevisionDescriptor()

		then: "The information about file revision should be successfully aggregated"
		fileRevisionDescriptor.getResourceId() == RESOURCE_ID
		fileRevisionDescriptor.getFileId() == FILE_ID
		fileRevisionDescriptor.getFileName() == FILE_NAME
		fileRevisionDescriptor.getFileSize() == FILE_SIZE
		fileRevisionDescriptor.getLastModification() == LAST_MODIFICATION_MOMENT
	}

	def "Scenario: download empty range collection"() {
		FileRevisionDescriptor fileRevisionDescriptorReceivedOnStart
		FileRevisionDescriptor fileRevisionDescriptorReceivedOnError
		UnsatisfiableRangeFormatException exception;

		given: "The file revision accessor"
		RevisionPointer revisionPointer = createRevisionPointer(Optional.of(RESOURCE_ID), Optional.empty())
		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())
		fileUploadHistory.findFileUploadForResource(RESOURCE_ID) >> Optional.of(createFileUpload())
		fileStorage.getFileDescriptor(FILE_ID) >> createFileRescriptor()
		FileRevisionAccessor fileRevisionAccessor = new FileRevisionAccessor(revisionPointer, fileStorage, fileUploadHistory, resourceRepository)

		and: "The download visitor"
		MultiRangeDownloadVisitor downloadVisitor = Mock(MultiRangeDownloadVisitor)

		when: "The download command is received for empty ranges count"
		fileRevisionAccessor.download(Collections.emptyList(), destintationPoint, downloadVisitor).downloadFile()

		then: "The download visitor should be notified about start"
		1 * downloadVisitor.onDownloadStart(_) >> {fileRevisionDescriptorReceivedOnStart = it[0]}

		and: "The download visitor should be notified about error"
		1 * downloadVisitor.onDownloadError(_, _) >> {
			fileRevisionDescriptorReceivedOnError = it[0]
			exception = it[1]
		}

		and: "The download visitor shouldn't be notified about end of process"
		0 * downloadVisitor.onDownloadComplete(_)

		and: "The received descriptors on start and on error should be the same object instance"
		fileRevisionDescriptorReceivedOnStart.is(fileRevisionDescriptorReceivedOnError)

		and: "The unsatisfiable range format exception should be received"
		exception.getContext() == Constants.CONTEXT
		exception.getErrorCode() == UnsatisfiableRangeFormatException.ERROR_CODE
		exception.getSeverity() == Severity.BUSINESS
	}

	def "Scenario: multi-range download with error on one of range"() {
		Exception exception = new Exception("SOME ERROR")
		FileRevisionDescriptor fileRevisionDescriptorReceivedOnStart
		FileRevisionDescriptor fileRevisionDescriptorReceivedOnFirstSuccess
		FileRevisionDescriptor fileRevisionDescriptorReceivedOnSecondError
		Fragment downloadedFragment;

		given: "The file revision accessor"
		RevisionPointer revisionPointer = createRevisionPointer(Optional.of(RESOURCE_ID), Optional.empty())
		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())
		fileUploadHistory.findFileUploadForResource(RESOURCE_ID) >> Optional.of(createFileUpload())
		fileStorage.getFileDescriptor(FILE_ID) >> createFileRescriptor()
		FileRevisionAccessor fileRevisionAccessor = new FileRevisionAccessor(revisionPointer, fileStorage, fileUploadHistory, resourceRepository)

		and: "The ranges collection of 3 range"
		Collection<Range> ranges = [createRange(0, 0), createRange(1, 1), createRange(2, 2)]

		and: "The download visitor"
		MultiRangeDownloadVisitor downloadVisitor = Mock(MultiRangeDownloadVisitor)

		and: "The first range is completed successfully"
		fileStorage.downloadFileRange(FILE_ID, destintationPoint, _, 0L, 1L) >> {
			CompletionCallback callback = it[2]
			callback.onSuccess()
		}

		and: "The second range is completed with failure"
		fileStorage.downloadFileRange(FILE_ID, destintationPoint, _, 1L, 1L) >> {
			CompletionCallback callback = it[2]
			callback.onError(exception)
		}

		when: "The download command is happened"
		fileRevisionAccessor.download(ranges, destintationPoint, downloadVisitor).downloadFile()

		then: "The download visitor should be notified about start"
		1 * downloadVisitor.onDownloadStart(_) >> {fileRevisionDescriptorReceivedOnStart = it[0]}

		and: "The download visitor should be notified about first range sucessful download"
		1 * downloadVisitor.onFragmentDownloadComplete(_, _) >> {
			fileRevisionDescriptorReceivedOnFirstSuccess = it[0]
			downloadedFragment = it[1]
		}
		
		downloadedFragment.getStart() == 0L
		downloadedFragment.getSize() == 1L

		and: "The download visitor should be notified about error"
		1 * downloadVisitor.onDownloadError(_, exception) >> {fileRevisionDescriptorReceivedOnSecondError = it[0]}

		and: "The download visitor shouldn't be notified about end of process"
		0 * downloadVisitor.onDownloadComplete(_)

		and: "The ranges after failure shouldn't be requested"
		0 * fileStorage.downloadFileRange(FILE_ID, destintationPoint, _, 2L, 1L);

		and: "The received download descriptors should be the same instance"
		fileRevisionDescriptorReceivedOnFirstSuccess
				.is(fileRevisionDescriptorReceivedOnFirstSuccess)
				
		fileRevisionDescriptorReceivedOnFirstSuccess
				.is(fileRevisionDescriptorReceivedOnSecondError)
	}
	
	def "Scenario: multi-range successful download"() {
		FileRevisionDescriptor fileRevisionDescriptorReceivedOnStart
		FileRevisionDescriptor fileRevisionDescriptorReceivedOnFirstSuccess
		FileRevisionDescriptor fileRevisionDescriptorReceivedOnSecondSuccess
		FileRevisionDescriptor fileRevisionDescriptorReceivedOnThirdSuccess
		FileRevisionDescriptor fileRevisionDescriptorReceivedOnEnd
		Fragment firstDownloadedFragment;
		Fragment secondDownloadedFragment;
		Fragment thirdDownloadedFragment;

		given: "The file revision accessor"
		RevisionPointer revisionPointer = createRevisionPointer(Optional.of(RESOURCE_ID), Optional.empty())
		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())
		fileUploadHistory.findFileUploadForResource(RESOURCE_ID) >> Optional.of(createFileUpload())
		fileStorage.getFileDescriptor(FILE_ID) >> createFileRescriptor()
		FileRevisionAccessor fileRevisionAccessor = new FileRevisionAccessor(revisionPointer, fileStorage, fileUploadHistory, resourceRepository)

		and: "The ranges collection of 3 range"
		Collection<Range> ranges = [createRange(0, 0), createRange(1, 1), createRange(2, 2)]

		and: "The download visitor"
		MultiRangeDownloadVisitor downloadVisitor = Mock(MultiRangeDownloadVisitor)

		and: "All ranges downloads are completed successfully"
		fileStorage.downloadFileRange(FILE_ID, destintationPoint, _, _, _) >> {
			CompletionCallback callback = it[2]
			callback.onSuccess()
		}
		
		when: "The download command is happened"
		fileRevisionAccessor.download(ranges, destintationPoint, downloadVisitor).downloadFile()

		then: "The download visitor should be notified about start"
		1 * downloadVisitor.onDownloadStart(_) >> {fileRevisionDescriptorReceivedOnStart = it[0]}

		and: "The download visitor should be notified about first range sucessful download"
		1 * downloadVisitor.onFragmentDownloadComplete(_, _) >> {
			fileRevisionDescriptorReceivedOnFirstSuccess = it[0]
			firstDownloadedFragment = it[1]
		}
		
		firstDownloadedFragment.getStart() == 0L
		firstDownloadedFragment.getSize() == 1L
		
		and: "The download visitor should be notified about second range sucessful download"
		1 * downloadVisitor.onFragmentDownloadComplete(_, _) >> {
			fileRevisionDescriptorReceivedOnSecondSuccess = it[0]
			secondDownloadedFragment = it[1]
		}
		
		secondDownloadedFragment.getStart() == 1L
		secondDownloadedFragment.getSize() == 1L
		
		and: "The download visitor should be notified about third range sucessful download"
		1 * downloadVisitor.onFragmentDownloadComplete(_, _) >> {
			fileRevisionDescriptorReceivedOnThirdSuccess = it[0]
			thirdDownloadedFragment = it[1]
		}
		
		thirdDownloadedFragment.getStart() == 2L
		thirdDownloadedFragment.getSize() == 1L
		
		and: "The download visitor should be notified about end of process"
		1 * downloadVisitor.onDownloadComplete(_) >> {fileRevisionDescriptorReceivedOnEnd = it[0]} 
		
		and: "The download visitor shouldn't be notified about errors"
		0 * downloadVisitor.onDownloadError(_, _)

		and: "The received download descriptors should be the same instance"
		fileRevisionDescriptorReceivedOnStart
				.is(fileRevisionDescriptorReceivedOnFirstSuccess)
				
		fileRevisionDescriptorReceivedOnFirstSuccess
				.is(fileRevisionDescriptorReceivedOnSecondSuccess)
				
		fileRevisionDescriptorReceivedOnFirstSuccess
				.is(fileRevisionDescriptorReceivedOnThirdSuccess)
				
		fileRevisionDescriptorReceivedOnFirstSuccess
				.is(fileRevisionDescriptorReceivedOnEnd)
	}
	
	
	def "Scenario: download whole file successfully"() {
		FileRevisionDescriptor fileRevisionDescriptorReceivedOnStart
		FileRevisionDescriptor fileRevisionDescriptorReceivedOnEnd
		given: "The file revision accessor"
		RevisionPointer revisionPointer = createRevisionPointer(Optional.of(RESOURCE_ID), Optional.empty())
		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())
		fileUploadHistory.findFileUploadForResource(RESOURCE_ID) >> Optional.of(createFileUpload())
		fileStorage.getFileDescriptor(FILE_ID) >> createFileRescriptor()
		FileRevisionAccessor fileRevisionAccessor = new FileRevisionAccessor(revisionPointer, fileStorage, fileUploadHistory, resourceRepository)
		
		and: "The download visitor"
		DownloadVisitor downloadVisitor = Mock(DownloadVisitor)
		
		and: "The file download from storage is completed with success"
		fileStorage.downloadFile(FILE_ID, destintationPoint, _) >> {
			CompletionCallback callback = it[2]
			callback.onSuccess()
		}
		
		when: "The download whole file command is happened"
		fileRevisionAccessor.download(destintationPoint, downloadVisitor).downloadFile()
		
		then: "The download visitor should be notified about start"
		1 * downloadVisitor.onDownloadStart(_) >> {fileRevisionDescriptorReceivedOnStart = it[0]}
		
		and: "The download visitor should be notified about end of process"
		1 * downloadVisitor.onDownloadComplete(_) >> {fileRevisionDescriptorReceivedOnEnd = it[0]} 
		
		and: "The download visitor shouldn't be notified about errors"
		0 * downloadVisitor.onDownloadError(_, _)
		
		and: "The received download descriptors should be the same instance"
		fileRevisionDescriptorReceivedOnStart.is(fileRevisionDescriptorReceivedOnEnd)
	}
	
	def "Scenario: download whole file with failure"() {
		Exception error = new Exception("ERROR!!!");
		FileRevisionDescriptor fileRevisionDescriptorReceivedOnStart
		FileRevisionDescriptor fileRevisionDescriptorReceivedOnError
		given: "The file revision accessor"
		RevisionPointer revisionPointer = createRevisionPointer(Optional.of(RESOURCE_ID), Optional.empty())
		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())
		fileUploadHistory.findFileUploadForResource(RESOURCE_ID) >> Optional.of(createFileUpload())
		fileStorage.getFileDescriptor(FILE_ID) >> createFileRescriptor()
		FileRevisionAccessor fileRevisionAccessor = new FileRevisionAccessor(revisionPointer, fileStorage, fileUploadHistory, resourceRepository)
		
		and: "The download visitor"
		DownloadVisitor downloadVisitor = Mock(DownloadVisitor)
		
		and: "The file download from storage is completed with error"
		fileStorage.downloadFile(FILE_ID, destintationPoint, _) >> {
			CompletionCallback callback = it[2]
			callback.onError(error)
		}
		
		when: "The download whole file command is happened"
		fileRevisionAccessor.download(destintationPoint, downloadVisitor).downloadFile()
		
		then: "The download visitor should be notified about start"
		1 * downloadVisitor.onDownloadStart(_) >> {fileRevisionDescriptorReceivedOnStart = it[0]}
		
		and: "The download visitor should be notified about end of process"
		0 * downloadVisitor.onDownloadComplete(_)
		
		and: "The download visitor shouldn't be notified about errors"
		1 * downloadVisitor.onDownloadError(_, error) >> {fileRevisionDescriptorReceivedOnError = it[0]}
		
		and: "The received download descriptors should be the same instance"
		fileRevisionDescriptorReceivedOnStart.is(fileRevisionDescriptorReceivedOnError)
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
				.uploadMoment(Instant.now())
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
