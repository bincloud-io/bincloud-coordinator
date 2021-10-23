package io.bcs.storage.domain.model

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.CountDownLatch

import io.bce.domain.errors.ApplicationException
import io.bce.domain.errors.ErrorDescriptor.ErrorSeverity
import io.bce.promises.Promise.ErrorHandler
import io.bcs.common.domain.model.io.transfer.CompletionCallback
import io.bcs.common.domain.model.io.transfer.DestinationPoint
import io.bcs.common.domain.model.io.transfer.SourcePoint
import io.bcs.common.domain.model.io.transfer.TransferingScheduler
import io.bcs.common.domain.model.io.transfer.Transmitter
import io.bcs.storage.domain.model.FileRevision.ContentUploader
import io.bcs.storage.domain.model.contexts.FileDownloadingContext
import io.bcs.storage.domain.model.contexts.FileUploadingContext
import io.bcs.storage.domain.model.contracts.FileDescriptor
import io.bcs.storage.domain.model.states.DistributionFileRevisionState
import io.bcs.storage.domain.model.states.FileAlreadyExistsException
import io.bcs.storage.domain.model.states.FileHasAlreadyBeenUploadedException
import io.bcs.storage.domain.model.states.FileRevisionStatus
import spock.lang.Specification

class DistributionFileSpec extends Specification {
	private static final String FILE_REVISION_NAME = "12345"
	private static final String FILE_NAME = "file.txt"
	private static final String FILE_MEDIA_TYPE = "application/media"
	private static final String FILE_DISPOSITION = "inline"
	private static final Instant TIMESTAMP_INITIAL_POINT = Instant.now()
	private static final Instant TIMESTAMP_NEXT_POINT = TIMESTAMP_INITIAL_POINT.plus(1, ChronoUnit.MILLIS)


	private SourcePoint sourcePoint
	private DestinationPoint destinationPoint
	private FilesystemAccessor filesystem
	private TransferingScheduler scheduler
	private CompletionCallback completionCallback;

	def setup() {
		this.sourcePoint = Stub(SourcePoint)
		this.destinationPoint = Stub(DestinationPoint)
		this.filesystem = Mock(FilesystemAccessor)
		this.scheduler = Mock(TransferingScheduler)
		this.completionCallback = Mock(CompletionCallback)
	}

	def "Scenario: file can not be created in the distribution state"() {
		given: "The file in distribution state"
		FileRevision file =  createDistributionFile()

		when: "The file creation is requested"
		file.createFile(filesystem)

		then: "The file has been disposed should be thrown"
		ApplicationException error = thrown(FileAlreadyExistsException)
		error.getContextId() == FileAlreadyExistsException.CONTEXT
		error.getErrorCode() == FileAlreadyExistsException.ERROR_CODE
		error.getErrorSeverity() == ErrorSeverity.BUSINESS

		and: "The file status should not be changed"
		file.status == FileRevisionStatus.DISTRIBUTION.name()
	}

	def "Scenario: file can not be uploaded in the distribution state"() {
		ApplicationException thrownError
		CountDownLatch latch = new CountDownLatch(1)
		
		given: "The file in distribution state"
		FileRevision file =  createDistributionFile()

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
			.error(ApplicationException, errorHandler)
		latch.await()

		then: "The file has been disposed should be thrown"
		thrownError.getContextId() == FileHasAlreadyBeenUploadedException.CONTEXT
		thrownError.getErrorCode() == FileHasAlreadyBeenUploadedException.ERROR_CODE
		thrownError.getErrorSeverity() == ErrorSeverity.BUSINESS

		and: "The file status should not be changed"
		file.status == FileRevisionStatus.DISTRIBUTION.name()
	}

	def "Scenario: file content can not be downloaded in the disposed state"() {
		given: "The file in distribution state"
		FileRevision file =  createDistributionFile()

		and: "There is access on read to the file on a file system for range 10..20"
		1 * filesystem.getAccessOnRead(file.getRevisionName().getFilesystemName(), 10, 20) >> sourcePoint

		and: "The file transfering can be scheduled"
		scheduler.schedule(sourcePoint, destinationPoint, _) >> {
			CompletionCallback callback = it[2]
			return Stub(Transmitter) {
				start() >> {
					callback.onSuccess()
				}
			}
		}

		when: "The file content 10..20 downloading is requested"
		FileDownloadingContext downloadingContext = createDownloadingContext()
		file.downloadFileContent(downloadingContext, 10, 20)
		FileDescriptor fileDescriptor = file.getDescriptor()

		then: "The transmission should be completed"
		1 * completionCallback.onSuccess()
		
		and: "The modification time should be changed"
		fileDescriptor.lastModification == TIMESTAMP_NEXT_POINT

		and: "The status should not be changed"
		fileDescriptor.status == FileRevisionStatus.DISTRIBUTION.name()
		
		and: "The filesystem name should be generated"
		fileDescriptor.getRevisionName() == FILE_REVISION_NAME
		
		and: "The file name should be got from file attributes"
		fileDescriptor.getFileName() == FILE_NAME
		
		and: "The media type should be got from file attributes"
		fileDescriptor.getMediaType() == FILE_MEDIA_TYPE
		
		and: "The content disposition should be got from file attributes"
		fileDescriptor.getContentDisposition() == FILE_DISPOSITION
	}

	def "Scenario: file distribution can not be started in the distribution state"() {
		given: "The disposed file"
		FileRevision file =  createDistributionFile()

		when: "The file distribution start is requested"
		file.startDistribution()

		then: "The file has been disposed should be thrown"
		ApplicationException error = thrown(FileHasAlreadyBeenUploadedException)
		error.getContextId() == FileHasAlreadyBeenUploadedException.CONTEXT
		error.getErrorCode() == FileHasAlreadyBeenUploadedException.ERROR_CODE
		error.getErrorSeverity() == ErrorSeverity.BUSINESS

		and: "The file status should not be changed"
		file.status == FileRevisionStatus.DISTRIBUTION.name()
	}
	
	private def createDownloadingContext() {
		return new FileDownloadingContext(destinationPoint, scheduler, filesystem, completionCallback)
	}

	def createDistributionFile() {
		return FileRevision.builder()
				.revisionName(new FileId(FILE_REVISION_NAME))
				.fileName(FILE_NAME)
				.mediaType(FILE_MEDIA_TYPE)
				.contentDisposition(FILE_DISPOSITION)
				.creationMoment(TIMESTAMP_INITIAL_POINT)
				.lastModification(TIMESTAMP_NEXT_POINT)
				.state(new DistributionFileRevisionState())
				.fileSize(0L)
				.build()
	}
}
