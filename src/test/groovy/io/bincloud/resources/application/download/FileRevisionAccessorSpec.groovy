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
import io.bincloud.resources.domain.model.contracts.download.Range
import io.bincloud.resources.domain.model.contracts.RevisionPointer
import io.bincloud.resources.domain.model.contracts.download.DownloadListener
import io.bincloud.resources.domain.model.contracts.download.FileRevisionDescriptor
import io.bincloud.resources.domain.model.contracts.download.Fragment
import io.bincloud.resources.domain.model.contracts.download.FileDownloader.FileDownloadRequest
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
		fileRevisionDescriptor.getDefaultContentDisposition() == "inline"
		fileRevisionDescriptor.getMediaType() == "application/octet-stream"
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
		fileRevisionDescriptor.getDefaultContentDisposition() == "inline"
		fileRevisionDescriptor.getMediaType() == "application/octet-stream"
		
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
