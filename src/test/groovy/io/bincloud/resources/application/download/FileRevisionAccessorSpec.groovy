package io.bincloud.resources.application.download

import java.time.Instant
import java.time.temporal.ChronoUnit

import io.bincloud.common.domain.model.io.transfer.DestinationPoint
import io.bincloud.files.domain.model.FileDescriptor
import io.bincloud.files.domain.model.contracts.FileStorage
import io.bincloud.resources.domain.model.Constants
import io.bincloud.resources.domain.model.Resource
import io.bincloud.resources.domain.model.ResourceRepository
import io.bincloud.resources.domain.model.contracts.DownloadVisitor
import io.bincloud.resources.domain.model.contracts.RevisionPointer
import io.bincloud.resources.domain.model.contracts.FileDownloader.FileDownloadContext
import io.bincloud.resources.domain.model.errors.ResourceDoesNotExistException
import io.bincloud.resources.domain.model.errors.ResourceDoesNotHaveUploadsException
import io.bincloud.resources.domain.model.errors.UnspecifiedResourceException
import io.bincloud.resources.domain.model.errors.UploadedFileDescriptorHasNotBeenFoundException
import io.bincloud.resources.domain.model.file.FileRevisionDescriptor
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

//	def "Scenario: download empty range collection"() {
//		
//		given: "The file revision accessor"
//		RevisionPointer revisionPointer = createRevisionPointer(Optional.of(RESOURCE_ID), Optional.empty())
//		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())
//		fileUploadHistory.findFileUploadForResource(RESOURCE_ID) >> Optional.of(createFileUpload())
//		fileStorage.getFileDescriptor(FILE_ID) >> createFileRescriptor()
//		FileRevisionAccessor fileRevisionAccessor = new FileRevisionAccessor(revisionPointer, fileStorage, fileUploadHistory, resourceRepository)
//
//		when: "The download command is received for empty ranges count"
//		fileRevisionAccessor.download(Collections.emptyList(), destintationPoint, )
//	}

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
