package io.bcs.storage.domain.model

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.CountDownLatch

import io.bce.domain.errors.ErrorDescriptor
import io.bce.domain.errors.ErrorDescriptor.ErrorSeverity
import io.bce.promises.Promise.ErrorHandler
import io.bcs.common.domain.model.io.transfer.CompletionCallback
import io.bcs.common.domain.model.io.transfer.DestinationPoint
import io.bcs.common.domain.model.io.transfer.SourcePoint
import io.bcs.common.domain.model.io.transfer.TransferingScheduler
import io.bcs.storage.domain.model.FileRevision.ContentUploader
import io.bcs.storage.domain.model.contexts.FileDownloadingContext
import io.bcs.storage.domain.model.contexts.FileUploadingContext
import io.bcs.storage.domain.model.contracts.FileDescriptor
import io.bcs.storage.domain.model.states.DraftFileRevisionState
import io.bcs.storage.domain.model.states.FileDoesNotExistException
import io.bcs.storage.domain.model.states.FileManagementException
import io.bcs.storage.domain.model.states.FileRevisionStatus
import spock.lang.Specification

class DraftFileSpec extends Specification {
	private static final String FILE_REVISION_NAME = "12345"
	private static final String FILE_NAME = "file.txt"
	private static final String FILE_MEDIA_TYPE = "application/media"
	private static final String FILE_DISPOSITION = "inline"
	private static final Instant TIMESTAMP_INITIAL_POINT = Instant.now()
	private static final Instant TIMESTAMP_NEXT_POINT = TIMESTAMP_INITIAL_POINT.plus(1, ChronoUnit.MILLIS)
	private FilesystemAccessor filesystem

	def setup() {
		this.filesystem = Mock(FilesystemAccessor)
	}


	def "Scenario: new file successfully created in the filesystem"() {
		given: "The draft file"
		FileRevision file = createInitialFile()
		
		when: "The file creation in the filesystem is requested"
		file.createFile(filesystem)
		FileDescriptor fileDescriptor = file.getDescriptor()

		then: "The creation request should be delegated to the filesystem"
		1 * filesystem.createFile(FILE_REVISION_NAME)

		and: "The file entity status should be changed to created"
		fileDescriptor.getStatus() == FileRevisionStatus.CREATED.name()

		and: "The last modification time should be updated"
		fileDescriptor.getLastModification() != TIMESTAMP_INITIAL_POINT
		
		and: "The filesystem name should be generated"
		fileDescriptor.getRevisionName() == FILE_REVISION_NAME
		
		and: "The file name should be got from file attributes"
		fileDescriptor.getFileName() == FILE_NAME
		
		and: "The media type should be got from file attributes"
		fileDescriptor.getMediaType() == FILE_MEDIA_TYPE
		
		and: "The content disposition should be got from file attributes"
		fileDescriptor.getContentDisposition() == FILE_DISPOSITION
	}

	def "Scenario: file can not be uploaded in the draft state"() {
		ErrorDescriptor thrownError
		CountDownLatch latch = new CountDownLatch(1)
		
		given: "The draft file"
		FileRevision file = createInitialFile()

		and: "The content uploader"
		ContentUploader contentUploader = Mock(ContentUploader)
		
		and: "The error handler"
		ErrorHandler errorHandler = {error -> 
			thrownError = error
			latch.countDown()
		}
		
		when: "The file uploading is requested"
		file
			.uploadContent(contentUploader)
			.error(FileManagementException, errorHandler)
		latch.await()

		then: "The file not exists error has been thrown"
		thrownError.getContextId() == FileManagementException.CONTEXT
		thrownError.getErrorCode() == FileDoesNotExistException.ERROR_CODE
		thrownError.getErrorSeverity() == ErrorSeverity.BUSINESS
	}

	def "Scenario: file can not be downloaded in the draft state"() {
		given: "The draft file"
		FileRevision file = createInitialFile()

		when: "The file downloading is requested"
		file.downloadFileContent(createDummyDownloadContext(), 0, 100)

		then: "The file not exists error should be thrown"
		FileDoesNotExistException thrownError = thrown(FileDoesNotExistException)
		thrownError.getContextId() == FileManagementException.CONTEXT
		thrownError.getErrorCode() == FileDoesNotExistException.ERROR_CODE
		thrownError.getErrorSeverity() == ErrorSeverity.BUSINESS
	}

	def "Scenario: file distribution can not be started in the draft state"() {
		given: "The draft file"
		FileRevision file = createInitialFile()

		when: "The file distribution start is requested"
		file.startDistribution()

		then: "The file not exists error should be thrown"
		FileDoesNotExistException thrownError = thrown(FileDoesNotExistException)
		thrownError.getContextId() == FileManagementException.CONTEXT
		thrownError.getErrorCode() == FileDoesNotExistException.ERROR_CODE
		thrownError.getErrorSeverity() == ErrorSeverity.BUSINESS
	}

	def createDummyDownloadContext() {
		return new FileDownloadingContext(Stub(DestinationPoint), Stub(TransferingScheduler), filesystem, Stub(CompletionCallback))
	}

	def createDummyUploadContext() {
		return new FileUploadingContext(1000L, Stub(SourcePoint), Stub(TransferingScheduler), filesystem, Stub(CompletionCallback))
	}

	def createInitialFile() {
		return FileRevision.builder()
				.revisionName(new FileId(FILE_REVISION_NAME))
				.fileName(FILE_NAME)
				.mediaType(FILE_MEDIA_TYPE)
				.contentDisposition(FILE_DISPOSITION)
				.creationMoment(TIMESTAMP_INITIAL_POINT)
				.lastModification(TIMESTAMP_INITIAL_POINT)
				.state(new DraftFileRevisionState())
				.fileSize(0L)
				.build()
	}
}
